package models.results;

import models.newserialization.*;

import java.util.*;

/**
 * Created by ilya
 */
public class InfoPattern extends SerializationType<Info> {

    private final LinkedHashMap<String, SerializationType<?>> field2type;

    private final LinkedHashMap<String, String> field2title;

    public InfoPattern(Object... values) {
        int n = values.length;

        if (n % 3 != 0)
            throw new IllegalArgumentException("Number of arguments should divide by 3");

        field2type = new LinkedHashMap<>();
        field2title = new LinkedHashMap<>();

        for (int i = 0; i < n; i += 3) {
            String field = (String) values[i];

            SerializationType<?> type = (SerializationType<?>) values[i + 1];
            String title = (String) values[i + 2];

            register(field, type, title);
        }
    }

    public InfoPattern(LinkedHashMap<String, SerializationType<?>> field2type, LinkedHashMap<String, String> field2title) {
        this.field2type = field2type;
        this.field2title = field2title;
    }

    public void register(String field, SerializationType type, String title) {
        field2type.put(field, type);
        field2title.put(field, title);
    }

    public Collection<String> getFields() {
        return field2title.keySet();
    }

    public String getTitle(String field) {
        return field2title.get(field);
    }

    public void write(Info value, Serializer serializer) {
        if (value == null)
            return;

        for (Map.Entry<String, SerializationType<?>> entry : field2type.entrySet()) {
            String field = entry.getKey();
            SerializationType type = entry.getValue();

            //noinspection unchecked
            type.write(serializer, field, value.get(field));
        }
    }

    @Override
    public void write(Serializer serializer, String field, Info value) {
        if (value == null) {
            serializer.writeNull(field);
            return;
        }

        Serializer subSerializer = serializer.getSerializer(field);
        write(value, subSerializer);
    }

    @Override
    public void write(ListSerializer serializer, Info value) {
        Serializer subSerializer = serializer.getSerializer();
        write(value, subSerializer);
    }

    public Info read(Deserializer deserializer) {
        Info map = new Info();

        for (Map.Entry<String, SerializationType<?>> entry : field2type.entrySet()) {
            String field = entry.getKey();
            SerializationType type = entry.getValue();

            map.put(field, type.read(deserializer, field));
        }

        return map;
    }

    @Override
    public Info read(Deserializer deserializer, String field) {
        if (deserializer.isNull(field))
            return null;
        return read(deserializer.getDeserializer(field));
    }

    @Override
    public Info read(ListDeserializer deserializer) {
        if (deserializer.nextIsNull()) {
            deserializer.readString(); //does not matter what null to read
            return null;
        }
        return read(deserializer.getDeserializer());
    }

    public static InfoPattern deserialize(Deserializer deserializer) {
        LinkedHashMap<String, SerializationType<?>> field2type = new LinkedHashMap<>();
        LinkedHashMap<String, String> field2title = new LinkedHashMap<>();

        for (String field : deserializer.fields()) {
            Deserializer fieldDeserializer = deserializer.getDeserializer(field);

            String typeName = fieldDeserializer.readString("type");
            String title = fieldDeserializer.readString("title");

            SerializationType type;
            switch (typeName) {
                case "int":
                    type = new BasicSerializationType<>(Integer.class);
                    break;
                case "long":
                    type = new BasicSerializationType<>(Long.class);
                    break;
                case "double":
                    type = new BasicSerializationType<>(Double.class);
                    break;
                case "text":
                    type = new BasicSerializationType<>(String.class);
                    break;
                case "boolean":
                    type = new BasicSerializationType<>(Boolean.class);
                    break;
                case "date":
                    type = new BasicSerializationType<>(Date.class);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type for serializable map: " + typeName);
            }

            field2type.put(field, type);
            field2title.put(field, title);
        }

        return new InfoPattern(field2type, field2title);
    }
}
