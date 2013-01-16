package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.forms.InputForm;
import play.Logger;
import play.cache.Cache;
import play.mvc.Http;

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

    public static final Event ERROR_EVENT = new Event(new MemoryStoredObject(
            "_id", "__no_event",
            "title", "Unknown event" //here is no HTTP context to use Messages
    ));

    private final String id;
    private final String title;
    private final List<Contest> contests;
    private final InputForm usersForm;
    private final InputForm editUserForm;

    private Event(StoredObject storedObject) {
        this.id = storedObject.getString("_id");
        this.title = storedObject.getString("title");

        this.contests = new ArrayList<>();
        List contestsConfig = storedObject.getList("contests");
        if (contestsConfig != null) {
            for (Object contestInfo : contestsConfig)
                contests.add(new Contest((StoredObject) contestInfo));
        }

        StoredObject users = storedObject.getObject("users");
        if (users != null) {
            usersForm = new InputForm("user", users);
            editUserForm = new InputForm("user_edit", users, "login", "email");
            editUserForm.setMessagesName("user"); //TODO invent something better
        } else {
            usersForm = null;
            editUserForm = null;
        }
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

    private static Event current(Http.Context ctx) {
        Event event = (Event) ctx.args.get("event");

        if (event == null) {
            //need to parse path because https://groups.google.com/forum/?fromgroups=#!topic/play-framework/sNFeqmd-mBQ
            String path = ctx.request().path();
            int firstSlash = path.indexOf('/');
            int secondSlash = path.indexOf('/', firstSlash + 1);
            if (firstSlash >= 0 && secondSlash >= 0)
                event = getInstance(path.substring(firstSlash + 1, secondSlash));

            if (event == null)
                event = ERROR_EVENT;

            ctx.args.put("event", event);
        }

        return event;
    }

    public static String currentId(Http.Context ctx) {
        Event current = current(ctx);
        return current == null ? null : current.getId();
    }

    public static Event current() {
        return current(Http.Context.current());
    }

    public static String currentId() {
        Event current = current();
        return current == null ? null : current.getId();
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

    public InputForm getEditUserForm() {
        return editUserForm;
    }

}
