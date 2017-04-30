package controllers.worker;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.Event;
import models.newserialization.*;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//TODO reimplement everything with workers: send email, upload user fields, etc.
public class Worker implements SerializableUpdatable {

    private ObjectId id = new ObjectId();
    private String eventId;
    private String name;
    private String description;
    private File log;
    private Date finish;

    private PrintStream logStream = null;

    public Worker() {}

    public Worker(String name, String description) {
        this.eventId = Event.currentId();
        this.name = name;
        this.description = description;
        this.log = getLog(name);
        this.finish = null;
    }

    private File getLog(String name) {
        File logsFolder = getWorkersFolder();
        return new File(logsFolder, name + "-" + id + ".log");
    }

    public ObjectId getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public File getLog() {
        return log;
    }

    public long getCreationTime() {
        return id.getDate().getTime();
    }

    public boolean isFinished() {
        return finish != null;
    }

    public Date getFinish() {
        return finish;
    }

    private static File getWorkersFolder() {
        File dataFolder = Event.current().getEventDataFolder();
        File workersFolder = new File(dataFolder, "_workers");
        //noinspection ResultOfMethodCallIgnored
        workersFolder.mkdir(); //the folder may already exist
        return workersFolder;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("_id", id);
        serializer.write("event_id", eventId);
        serializer.write("name", name);
        serializer.write("description", description);
        serializer.write("log", log.getAbsolutePath());
        serializer.write("finish", finish);
    }

    @Override
    public void update(Deserializer deserializer) {
        id = deserializer.readObjectId("_id");
        eventId = deserializer.readString("event_id");
        name = deserializer.readString("name");
        description = deserializer.readString("description");
        log = new File(deserializer.readString("log"));
        finish = deserializer.readDate("finish");
    }

    public void store() {
        MongoSerializer mongoSerializer = new MongoSerializer();
        serialize(mongoSerializer);
        MongoConnection.getWorkersCollection().save(mongoSerializer.getObject());
    }

    public static Worker load(DBObject object) {
        Worker result = new Worker();

        result.update(new MongoDeserializer(object));

        return result;
    }

    public static List<Worker> listWorkers(Event event) {
        DBObject query = new BasicDBObject("event_id", event.getId());

        List<Worker> result = new ArrayList<>();

        try (DBCursor cursor = MongoConnection.getWorkersCollection().find(query).sort(new BasicDBObject("_id", -1))) {
            while (cursor.hasNext()) {
                result.add(load(cursor.next()));
            }
        }

        return result;
    }

    public void execute(final Task task) {
        Akka.system().scheduler().scheduleOnce(
                Duration.Zero(),
                new Runnable() {
                    public void run() {
                        try (PrintStream logStream = new PrintStream(log, "UTF-8")) {
                            Worker.this.logStream = logStream;
                            store();
                            try {
                                task.run();
                            } catch (Exception e) {
                                logError("error in task execution", e);
                            }
                            Worker.this.finish = new Date();
                            store();
                        } catch (Exception e) {
                            Logger.error("Worker failed", e);
                        }
                    }
                },
                Akka.system().dispatcher()
        );
    }

    //logs

    public void logInfo(String message) {
        logInfo(message, null);
    }

    public void logInfo(String message, Throwable th) {
        log("info", message, th);
    }

    public void logWarn(String message) {
        logWarn(message, null);
    }

    public void logWarn(String message, Throwable th) {
        log("warn", message, th);
    }

    public void logError(String message) {
        logError(message, null);
    }

    public void logError(String message, Throwable th) {
        log("error", message, th);
    }

    public void log(String prefix, String message) {
        log(prefix, message, null);
    }

    public void log(String prefix, String message, Throwable th) {
        logStream.println('[' + prefix + "] " + message);
        if (th != null)
            th.printStackTrace(logStream);
        logStream.flush();
    }

    @FunctionalInterface
    public interface Task {
        void run() throws Exception;
    }
}