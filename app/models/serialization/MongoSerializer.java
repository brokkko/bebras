package models.serialization;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 12:30
 */
public class MongoSerializer implements Serializer {

    private final DBObject object;

    public MongoSerializer() {
        object = new BasicDBObject();
    }

    public MongoSerializer(DBObject object) {
        this.object = object;
    }

    @Override
    public void write(String field, Object value) {
        object.put(field, value);
    }

    @Override
    public Serializer getSerializer(String field) {
        MongoSerializer serializer = new MongoSerializer();
        object.put(field, serializer.getObject());
        return serializer;
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        MongoListSerializer serializer = new MongoListSerializer();
        object.put(field, serializer.getList());
        return serializer;
    }

    public DBObject getObject() {
        return object;
    }

    public void store(DBCollection collection) {
        collection.save(object);
    }
}
