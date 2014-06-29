package models.newserialization;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.08.13
 * Time: 12:26
 */
public class StringMapSerializationType<T> extends SerializationType<LinkedHashMap<String, T>>{

    private final SerializationType<T> subtype;

    public StringMapSerializationType(SerializationType<T> subtype) {
        this.subtype = subtype;
    }

    @Override
    public void write(Serializer serializer, String field, LinkedHashMap<String, T> value) {
        writeToSerializer(serializer.getSerializer(field), value);
    }

    @Override
    public void write(ListSerializer serializer, LinkedHashMap<String, T> value) {
        writeToSerializer(serializer.getSerializer(), value);
    }

    @Override
    public LinkedHashMap<String, T> read(Deserializer deserializer, String field) {
        if (deserializer.isNull(field))
            return new LinkedHashMap<>();

        return readFromDeserializer(deserializer.getDeserializer(field));
    }

    @Override
    public LinkedHashMap<String, T> read(ListDeserializer deserializer) {
        if (deserializer.nextIsNull()) {
            deserializer.readString(); //type does not matter
            return new LinkedHashMap<>();
        }

        return readFromDeserializer(deserializer.getDeserializer());
    }

    private void writeToSerializer(Serializer serializer, Map<String, T> value) {
        for (Map.Entry<String, T> keyValue : value.entrySet())
            subtype.write(serializer, keyValue.getKey(), keyValue.getValue());
    }

    private LinkedHashMap<String, T> readFromDeserializer(Deserializer deserializer) {
        LinkedHashMap<String, T> result = new LinkedHashMap<>();
        for (String field : deserializer.fields())
            result.put(field, subtype.read(deserializer, field));

        return result;
    }
}
