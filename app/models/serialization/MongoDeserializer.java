package models.serialization;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 21.03.13
 * Time: 0:13
 */
public class MongoDeserializer implements Deserializer {

    private DBObject object;

    public MongoDeserializer(DBObject object) {
        this.object = object;
    }

    @Override
    public int getInt(String field) {
        return (Integer) object.get(field);
    }

    @Override
    public Boolean getBoolean(String field) {
        return (Boolean) object.get(field);
    }

    @Override
    public String getString(String field) {
        return (String) object.get(field);
    }

    @Override
    public Object getObject(String field) {
        return object.get(field);
    }

    @Override
    public Deserializer getDeserializer(String field) {
        DBObject object = (DBObject) this.object.get(field);
        return object == null ? null : new MongoDeserializer(object);
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        BasicDBList objects = (BasicDBList) object.get(field);
        return objects == null ? null : new MongoListDeserializer(objects);
    }

    @Override
    public Set<String> fieldSet() {
        return object.keySet();
    }
}
