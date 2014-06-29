package models.newserialization;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by ilya
 */
public class MongoSerializer extends Serializer {

    private final DBObject object;

    public MongoSerializer() {
        object = new BasicDBObject();
    }

    public MongoSerializer(DBObject object) {
        this.object = object;
    }

    public DBObject getObject() {
        return object;
    }

    @Override
    public void write(String field, int value) {
        object.put(field, value);
    }

    @Override
    public void write(String field, long value) {
        object.put(field, value);
    }

    @Override
    public void write(String field, double value) {
        object.put(field, value);
    }

    @Override
    public void write(String field, boolean value) {
        object.put(field, value);
    }

    @Override
    public void write(String field, String value) {
        object.put(field, value);
    }

    @Override
    public void write(String field, Date value) {
        object.put(field, value);
    }

    @Override
    public void write(String field, ObjectId value) {
        object.put(field, value);
    }

    @Override
    public void write(String field, byte[] value) {
        object.put(field, value);
    }

    @Override
    public Serializer getSerializer(String field) {
        Object value = object.get(field);
        if (value != null && value instanceof DBObject)
            return new MongoSerializer((DBObject) value);
        //TODO do the same for list serializer

        MongoSerializer subSerializer = new MongoSerializer();
        object.put(field, subSerializer.getObject());
        return subSerializer;
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        MongoListSerializer subSerializer = new MongoListSerializer();
        object.put(field, subSerializer.getList());
        return subSerializer;
    }
}
