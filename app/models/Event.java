package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.fields.InputForm;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 13:34
 */
public class Event {

    private final String id;
    private final String title;
    private final List<Contest> contests;
    private final InputForm usersForm;

    private Event(StoredObject storedObject) {
        this.id = storedObject.getString("event_id");
        this.title = storedObject.getString("title");

        this.contests = new ArrayList<>();
        for (Object contestInfo : storedObject.getList("contests"))
            contests.add(new Contest((StoredObject) contestInfo));

        usersForm = new InputForm(storedObject.getObject("users"));
    }

    public static Event getInstance(final String eventId) {
        try {
            return Cache.getOrElse("event-" + eventId, new Callable<Event>() {
                @Override
                public Event call() throws Exception {
                    return createEventById(eventId);
                }
            }, 0);
        } catch (Exception e) {
            return null;
        }
    }

    private static Event createEventById(String eventId) throws Exception {
        DBCollection eventsCollection = MongoConnection.getEventsCollection();
        DBObject eventObject = eventsCollection.findOne(new BasicDBObject("event_id", eventId));
        if (eventObject == null)
            throw new Exception("No such collection");
        else
            return new Event(new MongoObject(eventsCollection.getName(), eventObject));
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Contest> getContests() {
        return contests;
    }

    public InputForm getUsersForm() {
        return usersForm;
    }
}
