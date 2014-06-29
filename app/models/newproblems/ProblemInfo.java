package models.newproblems;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.newserialization.Deserializer;
import models.newserialization.MongoDeserializer;
import models.newserialization.MongoSerializer;
import models.newserialization.SerializationTypesRegistry;
import org.bson.types.ObjectId;
import play.Logger;
import play.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.07.13
 * Time: 16:03
 */
public class ProblemInfo {

    public static final String FIELD_PROBLEM = "p";

    public static ProblemInfo get(final ObjectId pid) {
        //try to find problem in cache
        try {
            return Cache.getOrElse(getCacheKey(pid), new Callable<ProblemInfo>() {
                @Override
                public ProblemInfo call() {
                    DBObject db = MongoConnection.getProblemsCollection().findOne(new BasicDBObject("_id", pid));
                    if (db == null)
                        return null;

                    return new ProblemInfo(new MongoDeserializer(db));
                }
            }, 0);
        } catch (Exception e) {
            Logger.error("failed to get problem", e);
            return null; //TODO leads to null pointer exception
        }
    }

    private static String getCacheKey(ObjectId pid) {
        return "problem-" + pid;
    }

    public static ProblemInfo put(Problem problem) {
        DBObject newValue = new BasicDBObject();
        MongoSerializer newValueSerializer = new MongoSerializer(newValue);
        SerializationTypesRegistry.PROBLEM.write(newValueSerializer, FIELD_PROBLEM, problem);

        MongoConnection.getProblemsCollection().save(newValue);
        ObjectId id = (ObjectId) newValue.get("_id");

        return new ProblemInfo(id, problem);
    }

    private final ObjectId id;
    private final Problem problem;

    //TODO add additional information to store in DB


    public ProblemInfo(ObjectId id, Problem problem) {
        this.id = id;
        this.problem = problem;
    }

    public ProblemInfo(Deserializer deserializer) {
        id = deserializer.readObjectId("_id");
        problem = SerializationTypesRegistry.PROBLEM.read(deserializer, FIELD_PROBLEM);
    }

    public ObjectId getId() {
        return id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void store() {
        DBObject newValue = new BasicDBObject("_id", id);
        MongoSerializer newValueSerializer = new MongoSerializer(newValue);
        SerializationTypesRegistry.PROBLEM.write(newValueSerializer, FIELD_PROBLEM, problem);

        MongoConnection.getProblemsCollection().save(newValue);

        Cache.remove(getCacheKey(id));
    }
}