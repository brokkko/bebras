package models.newserialization;

import models.utils.Utils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.01.13
 * Time: 21:49
 */
public class MemoryDeserializer extends Deserializer {

    private Map<String, Object> map;

    public MemoryDeserializer(Object... values) {
        map = Utils.mapify(values);
    }

    public MemoryDeserializer(Map<String, Object> map) {
        this.map = map;
    }

    public void put(String field, Object value) {
        map.put(field, value);
    }

    @Override
    public Byte readByte(String field) {
        return (Byte) map.get(field);
    }

    @Override
    public Short readShort(String field) {
        return (Short) map.get(field);
    }

    @Override
    public Integer readInt(String field) {
        return (Integer) map.get(field);
    }

    @Override
    public Long readLong(String field) {
        return (Long) map.get(field);
    }

    @Override
    public Float readFloat(String field) {
        return (Float) map.get(field);
    }

    @Override
    public Double readDouble(String field) {
        return (Double) map.get(field);
    }

    @Override
    public Boolean readBoolean(String field) {
        return (Boolean) map.get(field);
    }

    @Override
    public Character readChar(String field) {
        return (Character) map.get(field);
    }

    @Override
    public String readString(String field) {
        return (String) map.get(field);
    }

    @Override
    public Date readDate(String field) {
        return (Date) map.get(field);
    }

    @Override
    public byte[] readByteArray(String field) {
        return (byte[]) map.get(field);
    }

    @Override
    public Deserializer getDeserializer(String field) {
        //noinspection unchecked
        Map<String, Object> subMap = (Map<String, Object>) map.get(field);
        return subMap == null ? null : new MemoryDeserializer(subMap);
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        //noinspection unchecked
        List<Object> list = (List<Object>) map.get(field);
        return list == null ? null : new MemoryListDeserializer(list);
    }

    @Override
    public Collection<String> fields() {
        return map.keySet();
    }

    @Override
    public boolean isNull(String field) {
        return map.get(field) == null;
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
