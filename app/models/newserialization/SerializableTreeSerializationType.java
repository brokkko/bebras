package models.newserialization;

import java.util.*;

/**
 * Created by ilya
 */
public class SerializableTreeSerializationType<T extends SerializableUpdatable> extends SerializationType<T> {

    private final Map<String, Class<? extends T>> name2class = new HashMap<>();
    private final Map<Class<? extends T>, String> class2name = new HashMap<>();

    public void registerClass(String name, Class<? extends T> clazz) {
        class2name.put(clazz, name);
        name2class.put(name, clazz);
    }

    @Override
    public void write(Serializer serializer, String field, T value) {
        if (value == null) {
            serializer.writeNull(field);
            return;
        }

        Serializer subSerializer = serializer.getSerializer(field);
        value.serialize(subSerializer);

        //noinspection SuspiciousMethodCalls
        subSerializer.write("type", class2name.get(value.getClass()));
    }

    @Override
    public void write(ListSerializer serializer, T value) {
        if (value == null) {
            serializer.write((String) null); //class does not really matters
            return;
        }

        Serializer subSerializer = serializer.getSerializer();
        value.serialize(subSerializer);

        //noinspection SuspiciousMethodCalls
        subSerializer.write("type", class2name.get(value.getClass())); //TODO what if no key
    }

    @Override
    public T read(Deserializer deserializer, String field) {
        if (deserializer.isNull(field))
            return null;

        Deserializer subSerializer = deserializer.getDeserializer(field);

        String type = subSerializer.readString("type");

        T value;
        try {
            value = name2class.get(type).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Deserialization Failed to instantiate an object of class " + type);
        }

        deserializer.update(field, value);

        return value;
    }

    @Override
    public T read(ListDeserializer deserializer) {
        if (deserializer.nextIsNull()) {
            deserializer.readString(); //the type String does not really matters
            return null;
        }

        Deserializer subDeserializer = deserializer.getDeserializer();

        String type = subDeserializer.readString("type");

        T value;
        try {
            value = name2class.get(type).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Deserialization Failed to instantiate an object of class " + type);
        }

        value.update(subDeserializer);

        return value;
    }

    public List<String> getTypes() {
        return new ArrayList<>(name2class.keySet());
    }

    public Class<? extends T> getClass(String type) {
        return name2class.get(type);
    }
}
