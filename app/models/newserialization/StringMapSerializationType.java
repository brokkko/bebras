package models.newserialization;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.08.13
 * Time: 12:26
 */
public class StringMapSerializationType<T> extends SerializationType<Map<String, T>>{

    private final SerializationType<T> subtype;

    public StringMapSerializationType(SerializationType<T> subtype) {
        this.subtype = subtype;
    }

    @Override
    public void write(Serializer serializer, String field, Map<String, T> value) {
        writeToSerializer(serializer.getSerializer(field), value);
    }

    @Override
    public void write(ListSerializer serializer, Map<String, T> value) {
        writeToSerializer(serializer.getSerializer(), value);
    }

    @Override
    public Map<String, T> read(Deserializer deserializer, String field) {
        if (deserializer.isNull(field))
            return new HashMap<>();

        return readFromDeserializer(deserializer.getDeserializer(field));
    }

    @Override
    public Map<String, T> read(ListDeserializer deserializer) {
        if (deserializer.nextIsNull()) {
            deserializer.readString(); //type does not matter
            return new HashMap<>();
        }

        return readFromDeserializer(deserializer.getDeserializer());
    }

    private void writeToSerializer(Serializer serializer, Map<String, T> value) {
        for (Map.Entry<String, T> keyValue : value.entrySet())
            subtype.write(serializer, keyValue.getKey(), keyValue.getValue());
    }

    private Map<String, T> readFromDeserializer(Deserializer deserializer) {
        Map<String, T> result = new HashMap<>();
        for (String field : deserializer.fields())
            result.put(field, subtype.read(deserializer, field));

        return result;
    }
}
