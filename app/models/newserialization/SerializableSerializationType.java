package models.newserialization;

/**
 * Created by ilya
 */
public class SerializableSerializationType<T extends SerializableUpdatable> extends SerializationType<T> {

    private final Class<T> clazz;

    public SerializableSerializationType(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void write(Serializer serializer, String field, T value) {
        serializer.write(field, value);
    }

    @Override
    public void write(ListSerializer serializer, T value) {
        serializer.write(value);
    }

    @Override
    public T read(Deserializer deserializer, String field) {
        if (deserializer.isNull(field))
            return null;

        T value;
        try {
            value = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Deserialization Failed to instantiate an object of class " + clazz);
        }

        return deserializer.update(field, value);
    }

    @Override
    public T read(ListDeserializer deserializer) {
        if (deserializer.nextIsNull()) {
            deserializer.readString(); //the type does not really matters
            return null;
        }

        T value;
        try {
            value = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Deserialization Failed to instantiate an object of class " + clazz);
        }

        return deserializer.update(value);
    }
}
