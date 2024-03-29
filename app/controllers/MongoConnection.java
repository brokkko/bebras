package controllers;

import com.mongodb.*;
import models.*;
import models.migration.*;
import models.newproblems.ProblemLink;
import play.Configuration;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Http;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 16:16
 */
public class MongoConnection {

    public static final String COLLECTION_NAME_CONFIG = "config";
    public static final String COLLECTION_NAME_USERS = "users";
    public static final String COLLECTION_NAME_EVENTS = "events";
    public static final String COLLECTION_NAME_PROBLEM_DIRS = "categories";
    public static final String COLLECTION_NAME_PROBLEMS = "problems";
    public static final String COLLECTION_NAME_ACTIVITY = "activity";
    public static final String COLLECTION_NAME_HTML_BLOCKS = "html_blocks";
    public static final String COLLECTION_NAME_DOMAINS = "domains";

    public static final String COLLECTION_MAILING_LIST = "mail_list";
    public static final String COLLECTION_MAILING_LIST_QUEUE = "mail_list_queue";
    public static final String COLLECTION_WORKERS = "workers";

    public static final String COLLECTION_RFI_LOG = "rfi_log";

    private static final Migrator[] migrators = new Migrator[] {
            null, //0
            null, //1
            new Migrator2(),
            new Migrator3(),
            new Migrator4(),
            new Migrator5(),
            new Migrator6(),
            new Migrator7(),
            new Migrator8(),
            new Migrator9(),
            new Migrator10()
    };

    public static DBCollection getConfigCollection() {
        return getCollection(COLLECTION_NAME_CONFIG);
    }

    public static DBCollection getUsersCollection() {
        return getCollection(COLLECTION_NAME_USERS);
    }

    public static DBCollection getEventsCollection() {
        return getCollection(COLLECTION_NAME_EVENTS);
    }

    public static DBCollection getProblemDirsCollection() {
        return getCollection(COLLECTION_NAME_PROBLEM_DIRS);
    }

    public static DBCollection getProblemsCollection() {
        return getCollection(COLLECTION_NAME_PROBLEMS);
    }

    public static DBCollection getActivityCollection() {
        return getCollection(COLLECTION_NAME_ACTIVITY);
    }

    public static DBCollection getHtmlBlocksCollection() {
        return getCollection(COLLECTION_NAME_HTML_BLOCKS);
    }

    public static DBCollection getMailingListCollection() {
        return getCollection(COLLECTION_MAILING_LIST);
    }

    public static DBCollection getMailingListQueueCollection() {
        return getCollection(COLLECTION_MAILING_LIST_QUEUE);
    }

    public static DBCollection getDomainsCollection() {
        return getCollection(COLLECTION_NAME_DOMAINS);
    }

    public static DBCollection getWorkersCollection() {
        return getCollection(COLLECTION_WORKERS);
    }

    public static DBCollection getRfiLogCollection() {
        return getCollection(COLLECTION_RFI_LOG);
    }

    public static List<DBCollection> getContestCollections() {
        DB db = getDb();
        Set<String> collectionNames = db.getCollectionNames();
        //filter

        List<DBCollection> contestCollections = new ArrayList<>();

        for (String name : collectionNames)
            if (name.startsWith(Contest.CONTEST_COLLECTION_NAME_PREFIX))
                contestCollections.add(getCollection(name));

        return contestCollections;
    }

    public static DBCollection getCollection(String collectionName) {
        DB answersDB = getDb();

        DBCollection collection = answersDB.getCollection(collectionName);
        Boolean collectionExists = (Boolean) Cache.get("collection-exists-" + collectionName);
        if (collectionExists == null)
            collectionExists = answersDB.collectionExists(collectionName);

        boolean needCreateIndexes = !collectionExists;

        if (needCreateIndexes) //TODO think about substitute with dbCollection.ensureIndex() on startup
            createIndexes(collection);

        Cache.set("collection-exists-" + collectionName, true);

        return collection;
    }

    private static DB getDb() {
        String dbname = Play.application().configuration().getString("mongodb.db");
        return getMongo().getDB(dbname); //TODO get rid of getDB
    }

    public static Object eval(String code, Object... args) {
        return getDb().eval(code, args);
    }

    public static boolean mayEnqueueEvents() {
        Http.Context ctx = Http.Context.current.get();
        return ctx != null && !ctx.args.containsKey("finalized");
    }

    public static void enqueueUserStorage(User user) {
        Map<String,Object> contextArgs = Http.Context.current().args;

        //noinspection unchecked
        Set<User> usersToStore = (Set<User>) contextArgs.get("users-to-store");

        if (usersToStore == null) {
            usersToStore = new HashSet<>();
            contextArgs.put("users-to-store", usersToStore);
        }

        usersToStore.add(user);
    }

    public static void storeEnqueuedUsers(Http.Context ctx) {
        Map<String,Object> contextArgs = ctx.args;

        //noinspection unchecked
        Set<User> usersToStore = (Set<User>) contextArgs.get("users-to-store");
        
        if (usersToStore != null)
            for (User user : usersToStore)
                user.serialize();
    }

    private static void createIndexes(DBCollection collection) {
        switch (collection.getName()) {
            case COLLECTION_NAME_USERS:
                DBObject eventAndLoginIndex = new BasicDBObject(User.FIELD_EVENT, 1);
                eventAndLoginIndex.put(User.FIELD_LOGIN, 1);
                collection.createIndex(eventAndLoginIndex, new BasicDBObject("unique", true));

                DBObject eventAndRoleIndex = new BasicDBObject(User.FIELD_EVENT, 1);
                eventAndRoleIndex.put(User.FIELD_USER_ROLE, 1);
                collection.createIndex(eventAndRoleIndex);

                DBObject eventAndEmailIndex = new BasicDBObject(User.FIELD_EVENT, 1);
                eventAndLoginIndex.put(User.FIELD_EMAIL, 1);
                collection.createIndex(eventAndEmailIndex); //TODO make unique (now there are exceptions in DB)

                collection.createIndex(new BasicDBObject(User.FIELD_CONFIRMATION_UUID, 1));
                collection.createIndex(new BasicDBObject(User.FIELD_REGISTRATION_UUID, 1));
                collection.createIndex(new BasicDBObject(User.FIELD_REGISTERED_BY, 1));
                break;
            case COLLECTION_NAME_ACTIVITY:
                collection.createIndex(new BasicDBObject(UserActivityEntry.FIELD_USER, 1));
                collection.createIndex(new BasicDBObject(UserActivityEntry.FIELD_IP, 1));
                break;
            case COLLECTION_NAME_PROBLEM_DIRS:
                collection.createIndex(new BasicDBObject(ProblemLink.FIELD_LINK, 1));
        }

        if (collection.getName().startsWith(Contest.CONTEST_COLLECTION_NAME_PREFIX)) {
            collection.createIndex(new BasicDBObject(Submission.USER_FIELD, 1));
            collection.createIndex(new BasicDBObject(Submission.SERVER_TIME_FIELD, 1));
            collection.createIndex(new BasicDBObject(Submission.LOCAL_TIME_FIELD, 1));

            BasicDBObject pidLocalTimeIndex = new BasicDBObject(Submission.PROBLEM_ID_FIELD, 1);
            pidLocalTimeIndex.put(Submission.LOCAL_TIME_FIELD, 1);
            collection.createIndex(pidLocalTimeIndex);
        }
    }

    private static MongoClient getMongo() {
        try {
            return Cache.getOrElse("mongo", new Callable<MongoClient>() {
                @Override
                public MongoClient call() throws Exception {
                    Configuration configuration = Play.application().configuration();

                    final String host = configuration.getString("mongodb.host");
                    final Integer configPort = configuration.getInt("mongodb.port");
                    final int port = configPort == null ? 27017 : configPort;

                    return new MongoClient(host, port);
                }
            }, 0);
        } catch (Exception e) {
            Logger.error("Failed to get mongo DB from cache", e);

            Configuration configuration = Play.application().configuration(); //TODO small code duplication

            final String host = configuration.getString("mongodb.host");
            final Integer configPort = configuration.getInt("mongodb.port");
            final int port = configPort == null ? 27017 : configPort;

            return new MongoClient(host, port);
        }
    }

    public static void migrate() {
        ServerConfiguration configuration = ServerConfiguration.getInstance();

        int dbVersion = configuration.getDbVersion();
        if (dbVersion >= ServerConfiguration.CURRENT_DB_VERSION)
            return;

        configuration.setMaintenanceMode(true);

        for (int ver = dbVersion + 1; ver <= ServerConfiguration.CURRENT_DB_VERSION; ver++) {
            Migrator migrator = migrators[ver];
            Logger.info("migrating from version " + (ver - 1) + " to version " + ver);
            migrator.migrate();
            configuration.setDbVersion(ver);
        }

        configuration.setMaintenanceMode(false);
    }

    public static void migrate(int index) {
        ServerConfiguration configuration = ServerConfiguration.getInstance();

        int dbVersion = configuration.getDbVersion();
        if (dbVersion != index - 1)
            return;

        configuration.setMaintenanceMode(true);

        Migrator migrator = migrators[index];
        Logger.info("migrating from version " + (index - 1) + " to version " + index);
        migrator.migrate();
        configuration.setDbVersion(index);

        configuration.setMaintenanceMode(false);
    }
}