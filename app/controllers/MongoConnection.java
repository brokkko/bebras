package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import models.Contest;
import models.Submission;
import models.User;
import models.UserActivityEntry;
import play.Configuration;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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

    public static DBCollection getCollection(String contestId) {
        String dbname = Play.application().configuration().getString("mongodb.db");
        DB answersDB = getMongo().getDB(dbname);

        DBCollection collection = answersDB.getCollection(contestId);
        boolean needCreateIndexes = !answersDB.collectionExists(contestId);

        if (needCreateIndexes)
            createIndexes(collection);

        return collection;
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


}