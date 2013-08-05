package models.newserialization;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import models.Utils;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 21.03.13
 * Time: 0:13
 */
public class MongoDeserializer extends Deserializer {

    private DBObject object;

    public MongoDeserializer(DBObject object) {
        this.object = object;
    }

    @Override
    public Integer readInt(String field) {
        Number n = (Number) object.get(field);
        return n == null ? null : n.intValue();
    }

    @Override
    public Long readLong(String field) {
        Number n = (Number) object.get(field);
        return n == null ? null : n.longValue();
    }

    @Override
    public Double readDouble(String field) {
        Number n = (Number) object.get(field);
        return n == null ? null : n.doubleValue();
    }

    @Override
    public Boolean readBoolean(String field) {
        return (Boolean) object.get(field);
    }

    @Override
    public String readString(String field) {
        return (String) object.get(field);
    }

    @Override
    public Date readDate(String field) {
        Object value = object.get(field);
        if (value == null || value instanceof Date)
            return (Date) value;

        return Utils.parseSimpleTime((String) value);
    }

    @Override
    public ObjectId readObjectId(String field) {
        return (ObjectId) object.get(field);
    }

    @Override
    public byte[] readByteArray(String field) {
        Object value = object.get(field);

        if (value instanceof byte[])
            return (byte[]) value;

        Binary binary = (Binary) value;
        return binary.getData();
    }

    @Override
    public Deserializer getDeserializer(String field) {
        DBObject subObject = (DBObject) object.get(field);
        return subObject == null ? null : new MongoDeserializer(subObject);
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        //noinspection unchecked
        BasicDBList list = (BasicDBList) object.get(field);
        return list == null ? null : new MongoListDeserializer(list);
    }

    @Override
    public Collection<String> fields() {
        return object.keySet();
    }

    @Override
    public boolean isNull(String field) {
        return object.get(field) == null;
    }
}