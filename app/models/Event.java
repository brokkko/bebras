package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import controllers.MongoConnection;
import controllers.actions.AuthenticatedAction;
import models.data.TableDescription;
import models.newserialization.*;
import models.results.*;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Http;
import plugins.Plugin;
import ru.ipo.kio.js.JsKioProblem;
import views.htmlblocks.HtmlBlock;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
    private Date registrationStart; //may be null, means start always
    private Date registrationFinish; //may be null, means never finishes
    private Date results;
    private Date restrictedResults; //this results means that we can not view problems
    private Date userInfoChangeClosed; //after this date users can not edit their info, only super admin may

    private CombinedTranslator resultTranslator = null; //cached translator that unions all translators
    private List<TableDescription> tables;

    private Map<String, UserRole> roles;
    private LinkedHashMap<String, Plugin> plugins; //linked map is needed because plugins initialization may matter

    private Map<String, InfoPattern> right2extraFields = new HashMap<>(); //extra fields for users
    private Map<String, Object> extraFields = new HashMap<>(); //extra fields for the event itself

    private String skin;
    private String domain;

    private boolean ssoEnabled;

    private void logClassLoader(ClassLoader cl) {
        List<ClassLoader> cls = new ArrayList<>();
        int cnt = 0;
        while (true) {
            cnt ++;
            cls.add(cl);
            cl = cl.getParent();
            if (cl == null || cnt > 20)
                break;
        }

        String collect = cls.stream().map(Object::toString).collect(Collectors.joining("[|||]"));
        Logger.debug(collect);
    }

    private Event(Deserializer deserializer) {
        logClassLoader(ClassLoader.getSystemClassLoader());
        logClassLoader(JsKioProblem.class.getClassLoader());
        logClassLoader(this.getClass().getClassLoader());

        this.id = deserializer.readString("_id");
        this.title = deserializer.readString("title");

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
        restrictedResults = deserializer.readDate("restricted results");
        userInfoChangeClosed = deserializer.readDate("user info closed");

        List<Translator> resultTranslators = SerializationTypesRegistry.list(SerializationTypesRegistry.TRANSLATOR).read(deserializer, "results translators");
        setResultTranslators(resultTranslators);

        tables = SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).read(deserializer, "tables");

        //read roles
        List<UserRole> roles = SerializationTypesRegistry.list(new SerializableSerializationType<>(UserRole.class)).read(deserializer, "roles");
        setRoles(roles);

        List<Plugin> plugins = SerializationTypesRegistry.list(SerializationTypesRegistry.PLUGIN).read(deserializer, "plugins");
        setPlugins(plugins);

        domain = deserializer.readString("domain");
        skin = deserializer.readString("skin", "default");

        ssoEnabled = deserializer.readBoolean("sso", false);

        //TODO enters site before confirmation
        //TODO choose where to go if authorized

        initPlugins();
    }

    private void initPlugins() {
        for (Plugin plugin : plugins.values())
            plugin.initEvent(this);
    }

    private void setRoles(List<UserRole> roles) {
        this.roles = new HashMap<>();
        for (UserRole role : roles)
            this.roles.put(role.getName(), role);
    }

    private void setResultTranslators(List<Translator> resultTranslators) {
        if (resultTranslators.size() == 0)
            resultTranslators.add(new EmptyTranslator());
        resultTranslator = new CombinedTranslator(resultTranslators);
    }

    private void setPlugins(List<Plugin> plugins) {
        this.plugins = new LinkedHashMap<>();
        for (Plugin plugin : plugins)
            this.plugins.put(plugin.getRef(), plugin);
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

        //serialize contests
        ListSerializer contestsSerializer = serializer.getListSerializer("contests");

        for (Contest contest : contests.values()) {
            Serializer contestSerializer = contestsSerializer.getSerializer();
            contest.serialize(contestSerializer);
        }

        serializer.write("registration start", registrationStart);
        serializer.write("registration finish", registrationFinish);
        serializer.write("results", results);
        serializer.write("restricted results", restrictedResults);
        serializer.write("user info closed", userInfoChangeClosed);

        SerializationTypesRegistry.list(SerializationTypesRegistry.TRANSLATOR).write(serializer, "results translators", resultTranslator.getTranslators());

        SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).write(serializer, "tables", tables);

        serializer.write("domain", domain);

        //write roles
        SerializationTypesRegistry.list(new SerializableSerializationType<>(UserRole.class)).write(serializer, "roles",
                new ArrayList<>(roles.values())
        );

        SerializationTypesRegistry.list(SerializationTypesRegistry.PLUGIN).write(serializer, "plugins", new ArrayList<>(plugins.values()));

        serializer.write("skin", skin);

        serializer.write("sso", ssoEnabled);
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

    public UserRole getRole(String name) {
        if ("ANON".equals(name))
            return UserRole.ANON;
        UserRole role = roles.get(name);
        return role == null ? UserRole.EMPTY : role;
    }

    public Collection<UserRole> getRoles() {
        return roles.values();
    }

    public Collection<UserRole> getRolesIncludingAnon() {
        List<UserRole> roles = new LinkedList<>(getRoles());
        roles.add(UserRole.ANON);
        return roles;
    }

    public UserRole getAnonymousRole() {
        return getRole("ANONYMOUS");
    }

    public String getDomain() {
        return domain;
    }

    public List<Plugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    public Plugin getPlugin(String id) {
        return plugins.get(id);
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

    public List<Contest> getContestsAvailableForUser() {
        User user = User.current();
        return getContestsAvailableForUser(user);
    }

    public List<Contest> getContestsAvailableForUser(User user) {
        //TODO do not return anon contests, make a separate method for anon contests
        if (user.hasEventAdminRight())
            return new ArrayList<>(getContests());

        List<Contest> c = new ArrayList<>(contests.size());
        for (Contest contest : contests.values()) {
            if (contest.isOnlyAdmin())
                continue;
            if (contest.isAvailableForUser(user))
                c.add(contest);
        }

        return c;
    }

    public Date getResults() {
        return results;
    }

    public Date getRestrictedResults() {
        return restrictedResults;
    }

    public Date getUserInfoChangeClosed() {
        return userInfoChangeClosed;
    }

    public List<? extends TableDescription> getTables() {
        return tables;
    }

    public TableDescription getTable(int index) {
        return index < 0 || index >= tables.size() ? null : tables.get(index);
    }

    //TODO works only after authentication
    public boolean resultsAvailable() {
        return getResults().before(AuthenticatedAction.getRequestTime()); //TODO getResults may be null ??
    }

    public File getEventDataFolder() {
        return getEventDataFolder(getId());
    }

    public static File getEventDataFolder(String eventId) {
        File folder = new File(Play.application().getFile("data"), eventId);
        folder.mkdirs();
        return folder;
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    public boolean registrationStarted() {
        return registrationStart == null || registrationStart.before(new Date()); //TODO get date from ... AuthenticatedAction
    }

    public boolean registrationFinished() {
        return registrationFinish != null && registrationFinish.before(new Date()); //TODO get date from ... AuthenticatedAction
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
        restrictedResults = deserializer.readDate("restricted results");
        userInfoChangeClosed = deserializer.readDate("user info closed");
        registrationStart = deserializer.readDate("registration start");
        registrationFinish = deserializer.readDate("registration finish");
        List<Translator> resultTranslators = SerializationTypesRegistry.list(SerializationTypesRegistry.TRANSLATOR).read(deserializer, "results translators");
        setResultTranslators(resultTranslators);
        tables = SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).read(deserializer, "tables");
        skin = deserializer.readString("skin", "");
        ssoEnabled = deserializer.readBoolean("sso", false);
        domain = deserializer.readString("domain");

        List<UserRole> roles = SerializationTypesRegistry.list(new SerializableSerializationType<>(UserRole.class)).read(deserializer, "roles");
        setRoles(roles);

        List<Plugin> plugins = SerializationTypesRegistry.list(SerializationTypesRegistry.PLUGIN).read(deserializer, "plugins");
        setPlugins(plugins);
    }

    public void invalidateCache() {
        invalidateCache(id);
    }

    public static void invalidateCache(String eventId) {
        Cache.remove(eventCacheKey(eventId));
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

    public HtmlBlock getHtmlBlock(String id) {
        return HtmlBlock.load(this.id, id);
    }

    public static HtmlBlock getGlobalHtmlBlock(String id) {
        return HtmlBlock.load("~global", id);
    }

    // plugins api

    public void registerExtraUserField(String right, String field, SerializationType type, String title) {
        InfoPattern infoPattern = right2extraFields.get(right);
        if (infoPattern == null) {
            infoPattern = new InfoPattern();
            right2extraFields.put(right, infoPattern);
        }

        infoPattern.register(field, type, title);
    }

    //may return null
    public InfoPattern getExtraUserFields(String role) {
        InfoPattern pattern = null;

        for (String right : getRole(role).getRights()) {
            pattern = InfoPattern.union(pattern, right2extraFields.get(right));
        }

        return pattern;
    }

    public void setExtraField(String field, Object value) {
        extraFields.put(field, value);
    }

    public Object getExtraField(String field) {
        return extraFields.get(field);
    }

    public Object getExtraField(String field, Object defaultValue) {
        Object result = extraFields.get(field);
        return result == null ? defaultValue : result;
    }

    //TODO move to settings
    public static String getOrganizationName() {
        Event event = Event.current();
        if (event != null && (event.getId().startsWith("bebras") || event.getId().startsWith("kio")))
            return "Центр информатизации образования «КИО»";
        return "Центр продуктивного обучения";
    }

    public String getSkin() {
        return skin;
    }

    public User createUser(String password, UserRole role, Info info, User register, boolean partialRegistration) {
        User user = new User();
        if (info == null)
            info = new Info();
        user.setInfo(info);
        user.setEvent(this);
        user.setPasswordHash(User.passwordHash(password));
        user.setConfirmed(true);
        user.setPartialRegistration(partialRegistration);
        user.setRole(role);
        user.setRegisteredBy(register);
        user.setWantAnnouncements(false); //TODO allow to change this

        try {
            user.serialize();
        } catch (MongoException exception) {
            Logger.warn("Failed to create user " + info.get("login"), exception);
            return null;
        }

        return user;
    }

    //TODO make it return Plugin of the needed type
    public <T extends Plugin> T getPluginByType(Class<T> pluginClass) {
        for (Plugin plugin : plugins.values())
            if (plugin.getClass().equals(pluginClass))
                return (T)plugin;

        return null;
    }
}
