package models;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 18:38
 */
public abstract class StoredObject {

    public abstract Object get(String field);
    public abstract void put(String field, Object value); //may put list here
    public abstract void store();
    public abstract Set<String> keySet();

    public String getString(String field) {
        return (String) get(field);
    }

    public Integer getInteger(String field) {
        return (Integer) get(field);
    }

    public Boolean getBoolean(String field) {
        return (Boolean) get(field);
    }

    public Date getDate(String field) {
        return (Date) get(field);
    }

    public StoredObject getObject(String field) {
        return (StoredObject) get(field);
    }

    public List getList(String field) {
        return (List) get(field);
    }

    public Map<String, Object> toMap() {
        Set<String> keys = keySet();
        HashMap<String, Object> map = new HashMap<>(keys.size());
        for (String key : keys)
            map.put(key, get(key));
        return map;
    }

}