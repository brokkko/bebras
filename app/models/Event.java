package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.forms.InputForm;
import play.Logger;
import play.cache.Cache;
import play.mvc.Http;
import play.mvc.PathBindable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 13:34
 */
public class Event implements PathBindable<Event> {

    private final String id;
    private final String title;
    private final List<Contest> contests;
    private final InputForm usersForm;

    private Event(StoredObject storedObject) {
        this.id = storedObject.getString("_id");
        this.title = storedObject.getString("title");

        this.contests = new ArrayList<>();
        List contestsConfig = storedObject.getList("contests");
        if (contestsConfig != null) {
            for (Object contestInfo : contestsConfig)
                contests.add(new Contest((StoredObject) contestInfo));
        }

        usersForm = new InputForm("user", storedObject.getObject("users"));
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
            Logger.error("Error while getting event '" + eventId + "'", e);
            return null;
        }
    }

    public static Event current() {
        return (Event) Http.Context.current().args.get("event");
    }

    public static void setCurrent(Event event) {
        Http.Context.current().args.put("event", event); //TODO find out how to bind with an event composition
    }

    private static Event createEventById(String eventId) throws Exception {
        DBCollection eventsCollection = MongoConnection.getEventsCollection();
        DBObject eventObject = eventsCollection.findOne(new BasicDBObject("_id", eventId));
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

    //event binding

    public Event() {
        id = null;
        title = null;
        contests = null;
        usersForm = null;
    }

    @Override
    public Event bind(String key, String path) {
        Event event = getInstance(path);
        if (event == null)
            throw new IllegalArgumentException("failed to bind"); //TODO how to properly report a binding error?
        return event;
    }

    @Override
    public String unbind(String s) {
        return getId();
    }

    @Override
    public String javascriptUnbind() {
        return "function(k,v) {return v}";
    }
}
