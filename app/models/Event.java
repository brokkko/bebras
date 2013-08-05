package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import controllers.actions.AuthenticatedAction;
import models.data.TableDescription;
import models.forms.*;
import models.newserialization.*;
import models.results.EmptyTranslator;
import models.results.InfoPattern;
import models.results.Translator;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Http;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 13:34
 */
public class Event {

    public static final Event ERROR_EVENT = new Event(new MemoryDeserializer(
            "_id", "__no_event",
            "title", "Unknown event" //here is no HTTP context to use Messages
    ));

    private String id;
    private String title;
    private LinkedHashMap<String, Contest> contests;
    private InputForm usersForm;
    private Date registrationStart; //may be null, means start always
    private Date registrationFinish; //may be null, means never finishes
    private Date results;
    private InfoPattern userInfoPattern;

    private Translator resultTranslator;
    private List<TableDescription> tables;

    private Event(Deserializer deserializer) {
        this.id = deserializer.readString("_id");
        this.title = deserializer.readString("title");

        usersForm = new SerializableSerializationType<>(InputForm.class).read(deserializer, "users");
        if (usersForm == null)
            usersForm = new InputForm();

        //get info pattern from field
        userInfoPattern = new InfoPattern();
        if (usersForm != null)
            for (InputField inputField : usersForm.getFields())
                if (inputField.isStore())
                    userInfoPattern.register(inputField.getName(), inputField.getInputTemplate().getType(), inputField.getInputTemplate().getTitle());

        //deserialize contests (they are not SerializableUpdatable)
        ListDeserializer contestsDeserializer = deserializer.getListDeserializer("contests");
        contests = new LinkedHashMap<>();
        if (contestsDeserializer != null)
            while (contestsDeserializer.hasMore()) {
                Deserializer contestDeserializer = contestsDeserializer.getDeserializer();
                Contest contest = Contest.deserialize(this, contestDeserializer);
                contests.put(contest.getId(), contest);
            }

        registrationStart = deserializer.readDate("registration start");
        registrationFinish = deserializer.readDate("registration finish");
        results = deserializer.readDate("results");

        resultTranslator = SerializationTypesRegistry.TRANSLATOR.read(deserializer, "results translator");
        if (resultTranslator == null)
            resultTranslator = new EmptyTranslator();


        tables = SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).read(deserializer, "tables");

        //TODO enters site before confirmation
        //TODO choose where to go if authorized
    }

    public static Event getInstance(final String eventId) {
        try {
            return Cache.getOrElse(eventCacheKey(eventId), new Callable<Event>() {
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

    private static String eventCacheKey(String eventId) {
        return "event-" + eventId;
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

    public void serialize(Serializer serializer) {
        serializer.write("_id", id);
        serializer.write("title", title);

        usersForm.serialize(serializer.getSerializer("users"));

        //serialize contests
        ListSerializer contestsSerializer = serializer.getListSerializer("contests");

        for (Contest contest : contests.values()) {
            Serializer contestSerializer = contestsSerializer.getSerializer();
            contest.serialize(contestSerializer);
        }

        serializer.write("registration start", registrationStart);
        serializer.write("registration finish", registrationFinish);
        serializer.write("results", results);

        SerializationTypesRegistry.TRANSLATOR.write(serializer, "results translator", resultTranslator);

        SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).write(serializer, "tables", tables);
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
        DBObject eventObject = loadEventDBObject(eventId);
        if (eventObject == null)
            throw new Exception("No such collection: " + eventId);
        else
            return new Event(new MongoDeserializer(eventObject));
    }

    private static DBObject loadEventDBObject(String eventId) {
        DBCollection eventsCollection = MongoConnection.getEventsCollection();
        return eventsCollection.findOne(new BasicDBObject("_id", eventId));
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Contest getContestById(String id) {
        return contests.get(id);
    }

    public void setContests(Collection<Contest> contests) {
        this.contests.clear();
        for (Contest contest : contests)
            this.contests.put(contest.getId(), contest);
    }

    public Collection<Contest> getContests() {
        return contests.values();
    }

    public Collection<Contest> getContestsAvailableForUser() {
        User user = User.current();
        if (user.getType() == UserType.EVENT_ADMIN)
            return getContests();

        List<Contest> c = new ArrayList<>(contests.size());
        for (Contest contest : contests.values())
            if (!contest.isOnlyAdmin())
                c.add(contest);

        return c;
    }

    public InputForm getUsersForm() {
        return usersForm;
    }

    public InputForm getEditUserForm() {
        return usersForm.filter(new InputForm.FieldFilter() {
            @Override
            public boolean accept(InputField field) {
                return !field.isSkipForEdit() && field.isStore();
            }
        });
    }

    public Date getResults() {
        return results;
    }

    public List<? extends TableDescription> getTables() {
        return tables;
    }

    public TableDescription getTable(int index) {
        return index < 0 || index >= tables.size() ? null : tables.get(index);
    }

    //TODO works only after authentication
    public boolean resultsAvailable() {
        return getResults().before(AuthenticatedAction.getRequestTime());
    }

    public File getEventDataFolder() {
        File folder = new File(Play.application().getFile("data"), getId());
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        return folder;
    }

    public boolean registrationStarted() {
        return registrationStart == null || registrationStart.before(new Date()); //TODO get date from ... AuthenticatedAction
    }

    public boolean registrationFinished() {
        return registrationFinish != null && registrationFinish.before(new Date()); //TODO get date from ... AuthenticatedAction
    }

    public InfoPattern getUserInfoPattern() {
        return userInfoPattern;
    }

    public Translator getResultTranslator() {
        return resultTranslator;
    }

    public InfoPattern getResultsInfoPattern() {
        return resultTranslator.getInfoPattern();
    }

    //form

    public void updateFromEventChangeForm(Deserializer deserializer) {
        title = deserializer.readString("title");
        results = deserializer.readDate("results");
        registrationStart = deserializer.readDate("registration start");
        registrationFinish = deserializer.readDate("registration finish");
    }

    public void invalidateCache() {
        Cache.remove(eventCacheKey(id));
    }

    public void store() {
        DBObject eventObject = loadEventDBObject(id);
        MongoSerializer serializer = new MongoSerializer(eventObject);
        serialize(serializer);
        MongoConnection.getEventsCollection().save(eventObject);

        invalidateCache();
    }

    public void addContest(Contest contest) {
        contests.put(contest.getId(), contest);
    }
}
