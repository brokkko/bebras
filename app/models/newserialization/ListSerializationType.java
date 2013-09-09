package models.newserialization;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.07.13
 * Time: 19:36
 */
public class ListSerializationType<T> extends SerializationType<List<T>> {
    //TODO code duplication with ArraySerializationType

    private final SerializationType<T> subtype;

    public ListSerializationType(SerializationType<T> subtype) {
        this.subtype = subtype;
    }

    @Override
    public void write(Serializer serializer, String field, List<T> values) {
        if (values == null) {
            serializer.writeNull(field);
            return;
        }

        ListSerializer ls = serializer.getListSerializer(field);
        for (T value : values)
            subtype.write(ls, value);
    }

    @Override
    public void write(ListSerializer serializer, List<T> values) {
        if (values == null) {
            serializer.writeNull();
            return;
        }

        ListSerializer ls = serializer.getListSerializer();
        for (T value : values)
            subtype.write(ls, value);
    }

    @Override
    public List<T> read(Deserializer deserializer, String field) {
        ListDeserializer ld = deserializer.getListDeserializer(field);

        if (ld == null)
            return new ArrayList<>();

        ArrayList<T> values = new ArrayList<>();
        while (ld.hasMore())
            values.add(subtype.read(ld));

        return values;
    }

    @Override
    public List<T> read(ListDeserializer deserializer) {
        ListDeserializer ld = deserializer.getListDeserializer();

        if (ld == null)
            return new ArrayList<>();

        ArrayList<T> values = new ArrayList<>();
        while (ld.hasMore())
            values.add(subtype.read(ld));

        return values;
    }
}
