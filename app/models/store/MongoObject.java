package models.store;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import controllers.MongoConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 15:08
 */
public class MongoObject extends StoredObject {

    private final String collection;
    private final DBObject object;

    public MongoObject(String collection) {
        this(collection, new BasicDBObject());
    }

    public MongoObject(String collection, DBObject object) {
        this.collection = collection;
        this.object = object;
    }

    @Override
    public Object get(String field) {
        return object.get(field);
    }

    @Override
    public void put(String field, Object value) {
        object.put(field, value);
    }

    @Override
    public void store() {
        MongoConnection.getCollection(collection).save(object);
    }

    @Override
    public Set<String> keySet() {
        return object.keySet();
    }
}