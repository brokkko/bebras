package models.newmodel;

import models.Utils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.01.13
 * Time: 21:49
 */
public class MemoryDeserializer implements Deserializer {

    private Map<String, Object> map;

    public MemoryDeserializer(Object... values) {
        map = Utils.mapify(values);
    }

    public MemoryDeserializer(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public int getInt(String field) {
        return (Integer) getObject(field);
    }

    @Override
    public Boolean getBoolean(String field) {
        return (Boolean) getObject(field);
    }

    @Override
    public String getString(String field) {
        return (String) getObject(field);
    }

    @Override
    public Object getObject(String field) {
        return map.get(field);
    }

    @Override
    public Deserializer getDeserializer(String field) {
        //noinspection unchecked
        Map<String, Object> map = (Map<String, Object>) getObject(field);
        return map == null ? null : new MemoryDeserializer(map);
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        //noinspection unchecked
        List<Object> list = (List<Object>) getObject(field);
        return list == null ? null : new MemoryListDeserializer(list);
    }

    @Override
    public Set<String> fieldSet() {
        return map.keySet();
    }
}
