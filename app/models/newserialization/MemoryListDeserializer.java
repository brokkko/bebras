package models.newserialization;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.04.13
 * Time: 14:07
 */
public class MemoryListDeserializer extends ListDeserializer {

    private final List<Object> list;
    private int index = 0;

    public MemoryListDeserializer(Object... list) {
        this.list = Arrays.asList(list);
    }

    public MemoryListDeserializer(List<Object> list) {
        this.list = list;
    }

    @Override
    public Byte readByte() {
        return (Byte) list.get(index++);
    }

    @Override
    public Short readShort() {
        return (Short) list.get(index++);
    }

    @Override
    public Integer readInt() {
        return (Integer) list.get(index++);
    }

    @Override
    public Long readLong() {
        return (Long) list.get(index++);
    }

    @Override
    public Float readFloat() {
        return (Float) list.get(index++);
    }

    @Override
    public Double readDouble() {
        return (Double) list.get(index++);
    }

    @Override
    public Boolean readBoolean() {
        return (Boolean) list.get(index++);
    }

    @Override
    public Character readChar() {
        return (Character) list.get(index++);
    }

    @Override
    public String readString() {
        return (String) list.get(index++);
    }

    @Override
    public byte[] readByteArray() {
        return (byte[]) list.get(index++);
    }

    @Override
    public Date readDate() {
        return (Date) list.get(index++);
    }

    @Override
    public Deserializer getDeserializer() {
        //noinspection unchecked
        Map<String, Object> object = (Map<String, Object>) list.get(index++);
        return object == null ? null : new MemoryDeserializer(object);
    }

    @Override
    public ListDeserializer getListDeserializer() {
        //noinspection unchecked
        List<Object> object = (List<Object>) list.get(index++);
        return object == null ? null : new MemoryListDeserializer(object);
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
