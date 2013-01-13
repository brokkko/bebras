package models;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.01.13
 * Time: 21:49
 */
public class MemoryStoredObject extends StoredObject {

    private Map<String, Object> map = new HashMap<>();

    public MemoryStoredObject(Object... values) {
        this(mapify(values));
    }

    public MemoryStoredObject(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object get(String field) {
        return wrap(map.get(field));
    }

    @Override
    public void put(String field, Object value) {
        map.put(field, value);
    }

    @Override
    public void store() {
        //do nothing
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    public static Map<String, Object> mapify(Object... values) {
        if (values.length % 2 != 0)
            throw new IllegalArgumentException("Number of arguments must be even");

        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < values.length; i += 2)
            map.put((String) values[i], values[i + 1]);

        return map;
    }

    public static List<Object> listify(Object... values) {
        return Arrays.asList(values);
    }

    //TODO code duplication with the wrap() method of MongoObject. Move wrapper to the StoredObject abstract class
    private static Object wrap(Object object) {
        if (object instanceof List) {
            ArrayList<Object> result = new ArrayList<>();
            for (Object o : (List) object)
                result.add(wrap(o));
            return result;
        }

        if (object instanceof Map)
            return new MemoryStoredObject((Map<String, Object>) object);

        return object;
    }
}
