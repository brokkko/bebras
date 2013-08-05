package models.newserialization;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ilya
 */
public class MemorySerializer extends Serializer {

    private final Map<String, Object> map;

    public MemorySerializer() {
        this.map = new HashMap<>();
    }

    public MemorySerializer(Map<String, Object> map) {
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public void write(String field, int value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, long value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, double value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, boolean value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, String value) {
        map.put(field, value);
    }

    @Override
    public Serializer getSerializer(String field) {
        MemorySerializer subSerializer = new MemorySerializer();
        map.put(field, subSerializer.getMap());
        return subSerializer;
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        MemoryListSerializer subSerializer = new MemoryListSerializer();
        map.put(field, subSerializer.getList());
        return subSerializer;
    }

    @Override
    public void write(String field, byte value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, short value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, float value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, Date value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, byte[] value) {
        map.put(field, value);
    }

    @Override
    public void write(String field, char value) {
        map.put(field, value);
    }
}
