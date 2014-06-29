package models.newserialization;

/**
 * Created by ilya
 */
public abstract class SerializationType<T> {

    public abstract void write(Serializer serializer, String field, T value);

    public abstract void write(ListSerializer serializer, T value);

    public abstract T read(Deserializer deserializer, String field);

    public abstract T read(ListDeserializer deserializer);
}
