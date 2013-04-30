package models.newmodel;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 21.03.13
 * Time: 0:17
 */
public class MongoListDeserializer implements ListDeserializer {
    private BasicDBList objects;
    private int position = 0;

    public MongoListDeserializer(BasicDBList objects) {
        this.objects = objects;
    }

    @Override
    public boolean hasMore() {
        return position < objects.size();
    }

    @Override
    public int getInt() {
        return (Integer) objects.get(position++);
    }

    @Override
    public boolean getBoolean() {
        return (Boolean) objects.get(position++);
    }

    @Override
    public String getString() {
        return (String) objects.get(position++);
    }

    @Override
    public Object getObject() {
        return objects.get(position);
    }

    @Override
    public Deserializer getDeserializer() {
        return new MongoDeserializer((DBObject) objects.get(position++));
    }

    @Override
    public ListDeserializer getListDeserializer() {
        return new MongoListDeserializer((BasicDBList) objects.get(position++));
    }
}
