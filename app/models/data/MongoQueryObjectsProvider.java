package models.data;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.07.13
 * Time: 15:20
 */
public class MongoQueryObjectsProvider implements ObjectsProvider<DBObject> {

    private DBCursor dbObjects = null;

    public MongoQueryObjectsProvider(DBCollection collection, DBObject query, DBObject sort) {
        dbObjects = collection.find(query);
        if (sort != null)
            dbObjects = dbObjects.sort(sort);
    }

    @Override
    public boolean hasNext() {
        return dbObjects.hasNext();
    }

    @Override
    public DBObject next() {
        return dbObjects.next();
    }

    @Override
    public void close() throws Exception {
        dbObjects.close();
    }
}
