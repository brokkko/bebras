package models.newmodel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.04.13
 * Time: 14:07
 */
public class MemoryListDeserializer implements ListDeserializer {

    private final List<Object> list;
    private int index = 0;

    public MemoryListDeserializer(Object... list) {
        this.list = Arrays.asList(list);
    }

    public MemoryListDeserializer(List<Object> list) {
        this.list = list;
    }

    @Override
    public boolean hasMore() {
        return index < list.size();
    }

    @Override
    public int getInt() {
        return (Integer) getObject();
    }

    @Override
    public boolean getBoolean() {
        return (Boolean) getObject();
    }

    @Override
    public String getString() {
        return (String) getObject();
    }

    @Override
    public Object getObject() {
        return list.get(index++);
    }

    @Override
    public Deserializer getDeserializer() {
        //noinspection unchecked
        Map<String, Object> object = (Map<String, Object>) getObject();
        return object == null ? null : new MemoryDeserializer(object);
    }

    @Override
    public ListDeserializer getListDeserializer() {
        //noinspection unchecked
        List<Object> object = (List<Object>) getObject();
        return object == null ? null : new MemoryListDeserializer(object);
    }
}
