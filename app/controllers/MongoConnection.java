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

    public static DBCollection getCollection(String contestId) {
        String dbname = Play.application().configuration().getString("mongodb.db");
        DB answersDB = getMongo().getDB(dbname);

//        boolean needCreateIndexes = ! answersDB.collectionExists(contestId);
//
//        if (needCreateIndexes) {
//            collection.createIndex(new BasicDBObject("userId", 1));
//            collection.createIndex(new BasicDBObject("problemId", 1));
//        }

        return answersDB.getCollection(contestId);
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
