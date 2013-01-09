package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import play.Configuration;
import play.Logger;
import play.Play;
import play.cache.Cache;

import java.net.UnknownHostException;
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

    private static final String COLLECTION_NAME_ANSWERS_PREFIX = "answers-";

    public static DBCollection getConfigCollection() {
        return getCollection(COLLECTION_NAME_CONFIG);
    }

    public static DBCollection getAnswersCollection(String eventId) {
        return getCollection(COLLECTION_NAME_ANSWERS_PREFIX + eventId);
    }

    public static DBCollection getUsersCollection() {
        return getCollection(COLLECTION_NAME_USERS);
    }

    public static DBCollection getEventsCollection() {
        return getCollection(COLLECTION_NAME_EVENTS);
    }

    public static DBCollection getCollection(String contestId) {
        String dbname = Play.application().configuration().getString("mongodb.db");
        DB answersDB = getMongo().getDB(dbname);

        DBCollection collection = answersDB.getCollection(contestId);
        boolean needCreateIndexes = ! answersDB.collectionExists(contestId);

        if (needCreateIndexes)
            createIndexes(collection);

        return collection;
    }

    private static void createIndexes(DBCollection collection) {
        switch (collection.getName()) {
            case COLLECTION_NAME_USERS:
                collection.createIndex(new BasicDBObject("registration_uuid", 1));
                collection.createIndex(new BasicDBObject("confirmation_uuid", 1));
                collection.createIndex(new BasicDBObject("event_id", 1));
                break;
        }
    }

    private static Mongo getMongo() {
        Configuration configuration = Play.application().configuration();

        final String host = configuration.getString("mongodb.host");
        Integer configPort = configuration.getInt("mongodb.port");
        final int port = configPort == null ? 27017 : configPort;

        try {
            return Cache.getOrElse("mongo", new Callable<Mongo>() {
                @Override
                public Mongo call() throws Exception {
                    return new Mongo(host, port);
                }
            }, 0);
        } catch (Exception e) {
            try {
                Logger.error("Failed to get mongo DB from cache", e);
                return new Mongo(host, port);
            } catch (UnknownHostException e1) {
                Logger.error("Failed to get mongo DB", e1);
                return null;
            }
        }
    }


}