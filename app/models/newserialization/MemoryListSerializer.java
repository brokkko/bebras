package models.newserialization;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ilya
 */
public class MemoryListSerializer extends ListSerializer {

    private final List<Object> list;

    public MemoryListSerializer() {
        this.list = new ArrayList<>();
    }

    public MemoryListSerializer(List<Object> list) {
        this.list = list;
    }

    public List<Object> getList() {
        return list;
    }

    @Override
    public void write(byte value) {
        list.add(value);
    }

    @Override
    public void write(short value) {
        list.add(value);
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
    public void write(float value) {
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
    public void write(byte[] value) {
        list.add(value);
    }

    @Override
    public void write(char value) {
        list.add(value);
    }

    @Override
    public Serializer getSerializer() {
        MemorySerializer subSerializer = new MemorySerializer();
        list.add(subSerializer.getMap());
        return subSerializer;
    }

    @Override
    public ListSerializer getListSerializer() {
        MemoryListSerializer subSerializer = new MemoryListSerializer();
        list.add(subSerializer.getList());
        return subSerializer;
    }

}
