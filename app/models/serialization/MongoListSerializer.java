package models.serialization;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 12:35
 */
public class MongoListSerializer implements ListSerializer {

    private BasicDBList list = new BasicDBList();

    @Override
    public void write(Object value) {
        list.add(value);
    }

    @Override
    public Serializer getSerializer() {
        MongoSerializer serializer = new MongoSerializer();
        list.add(serializer.getObject());
        return serializer;
    }

    @Override
    public ListSerializer getListSerializer() {
        MongoListSerializer serializer = new MongoListSerializer();
        list.add(serializer.getList());
        return serializer;
    }

    public DBObject getList() {
        return list;
    }
}
