package models.newserialization;

import com.mongodb.BasicDBList;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by ilya
 */
public class MongoListSerializer extends ListSerializer {

    private final BasicDBList list;

    public MongoListSerializer() {
        list = new BasicDBList();
    }

    public MongoListSerializer(BasicDBList list) {
        this.list = list;
    }

    public BasicDBList getList() {
        return list;
    }

    @Override
    public void write(int value) {
        list.add(value);
    }

    @Override
    public void write(long value) {
        list.add(value);
    }

    @Override
    public void write(double value) {
        list.add(value);
    }

    @Override
    public void write(boolean value) {
        list.add(value);
    }

    @Override
    public void write(String value) {
        list.add(value);
    }

    @Override
    public void write(Date value) {
        list.add(value);
    }

    @Override
    public void write(ObjectId value) {
        list.add(value);
    }

    @Override
    public void write(byte[] value) {
        list.add(value);
    }

    @Override
    public Serializer getSerializer() {
        MongoSerializer subSerializer = new MongoSerializer();
        list.add(subSerializer.getObject());
        return subSerializer;
    }

    @Override
    public ListSerializer getListSerializer() {
        MongoListSerializer subSerializer = new MongoListSerializer();
        list.add(subSerializer.getList());
        return subSerializer;
    }
}
