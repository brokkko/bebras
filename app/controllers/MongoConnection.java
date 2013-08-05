package controllers;

import com.mongodb.*;
import models.*;
import models.migration.Migrator;
import models.migration.Migrator2;
import models.migration.Migrator3;
import models.migration.Migrator4;
import models.newproblems.ProblemLink;
import play.Configuration;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Http;

import java.net.UnknownHostException;
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

    private static final Migrator[] migrators = new Migrator[]{
            null, //0
            null, //1
            new Migrator2(),
            new Migrator3(),
            new Migrator4()
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

    public static DBCollection getCollection(String contestId) {
        DB answersDB = getDb();

        DBCollection collection = answersDB.getCollection(contestId);
        boolean needCreateIndexes = !answersDB.collectionExists(contestId);

        if (needCreateIndexes) //TODO think about substitute with dbCollection.ensureIndex()
            createIndexes(collection);

        return collection;
    }

    private static DB getDb() {
        String dbname = Play.application().configuration().getString("mongodb.db");
        return getMongo().getDB(dbname);
    }

    public static boolean mayEnqueueEvents() {
        return Http.Context.current.get() != null;
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
                collection.createIndex(new BasicDBObject(User.FIELD_LOGIN, 1));
                collection.createIndex(new BasicDBObject(User.FIELD_CONFIRMATION_UUID, 1));
                collection.createIndex(new BasicDBObject(User.FIELD_REGISTRATION_UUID, 1));
                collection.createIndex(new BasicDBObject(User.FIELD_EVENT, 1));
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
        }
    }

    private static Mongo getMongo() {
        try {
            return Cache.getOrElse("mongo", new Callable<Mongo>() {
                @Override
                public Mongo call() throws Exception {
                    Configuration configuration = Play.application().configuration();

                    final String host = configuration.getString("mongodb.host");
                    final Integer configPort = configuration.getInt("mongodb.port");
                    final int port = configPort == null ? 27017 : configPort;

                    return new Mongo(host, port);
                }
            }, 0);
        } catch (Exception e) {
            try {
                Logger.error("Failed to get mongo DB from cache", e);

                Configuration configuration = Play.application().configuration(); //TODO small code duplication

                final String host = configuration.getString("mongodb.host");
                final Integer configPort = configuration.getInt("mongodb.port");
                final int port = configPort == null ? 27017 : configPort;

                return new Mongo(host, port);
            } catch (UnknownHostException e1) {
                Logger.error("Failed to get mongo DB", e1);
                return null;
            }
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