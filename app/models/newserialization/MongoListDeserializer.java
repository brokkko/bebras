package models.newserialization;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import models.utils.Utils;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by ilya
 */
public class MongoListDeserializer extends ListDeserializer {

    private final BasicDBList list;
    private int index = 0;

    public MongoListDeserializer(BasicDBList list) {
        this.list = list;
    }

    public BasicDBList getList() {
        return list;
    }

    @Override
    public Integer readInt() {
        Number number = (Number) list.get(index++);
        return number == null ? null : number.intValue();
    }

    @Override
    public Long readLong() {
        Number number = (Number) list.get(index++);
        return number == null ? null : number.longValue();
    }

    @Override
    public Double readDouble() {
        Number number = (Number) list.get(index++);
        return number == null ? null : number.doubleValue();
    }

    @Override
    public Boolean readBoolean() {
        return (Boolean) list.get(index++);
    }

    @Override
    public String readString() {
        return (String) list.get(index++);
    }

    @Override
    public Date readDate() {
        Object value = list.get(index++);
        if (value == null || value instanceof Date)
            return (Date) value;

        return Utils.parseSimpleTime((String) value);
    }

    @Override
    public ObjectId readObjectId() {
        return (ObjectId) list.get(index++);
    }

    @Override
    public byte[] readByteArray() {
        Object value = list.get(index++);

        if (value instanceof byte[])
            return (byte[]) value;

        Binary binary = (Binary) value;
        return binary.getData();
    }

    @Override
    public Deserializer getDeserializer() {
        DBObject subObject = (DBObject) list.get(index++);
        return subObject == null ? null : new MongoDeserializer(subObject);
    }

    @Override
    public ListDeserializer getListDeserializer() {
        //noinspection unchecked
        BasicDBList subList = (BasicDBList) list.get(index++);
        return subList == null ? null : new MongoListDeserializer(subList);
    }

    @Override
    public boolean hasMore() {
        return index < list.size();
    }

    @Override
    public boolean nextIsNull() {
        return list.get(index) == null;
    }
}
