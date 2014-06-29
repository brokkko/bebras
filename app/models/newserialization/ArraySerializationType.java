package models.newserialization;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.07.13
 * Time: 19:36
 */
public class ArraySerializationType<T> extends SerializationType<T[]> {

    private final SerializationType<T> subtype;

    public ArraySerializationType(SerializationType<T> subtype) {
        this.subtype = subtype;
    }

    @Override
    public void write(Serializer serializer, String field, T[] values) {
        ListSerializer ls = serializer.getListSerializer(field);
        for (T value : values)
            subtype.write(ls, value);
    }

    @Override
    public void write(ListSerializer serializer, T[] values) {
        ListSerializer ls = serializer.getListSerializer();
        for (T value : values)
            subtype.write(ls, value);
    }

    @Override
    public T[] read(Deserializer deserializer, String field) {
        ListDeserializer ld = deserializer.getListDeserializer(field);

        if (ld == null)
            //noinspection unchecked
            return (T[]) new Object[0];

        //TODO may be make size in List Deserializers
        ArrayList<T> values = new ArrayList<>();
        while (ld.hasMore())
            values.add(subtype.read(ld));

        //noinspection unchecked
        return (T[]) values.toArray();
    }

    @Override
    public T[] read(ListDeserializer deserializer) {
        ListDeserializer ld = deserializer.getListDeserializer();

        if (ld == null)
            //noinspection unchecked
            return (T[]) new Object[0];

        //TODO may be make size in List Deserializers
        ArrayList<T> values = new ArrayList<>();
        while (ld.hasMore())
            values.add(subtype.read(ld));

        //noinspection unchecked
        return (T[]) values.toArray();
    }
}
