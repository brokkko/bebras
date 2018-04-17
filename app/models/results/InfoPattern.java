package models.results;

import models.forms.InputField;
import models.forms.InputForm;
import models.forms.inputtemplate.InputTemplate;
import models.forms.validators.Validator;
import models.newserialization.*;
import play.twirl.api.Html;

import java.util.*;

/**
 * Created by ilya
 */
public class InfoPattern extends SerializationType<Info> {

    public static InfoPattern union(InfoPattern pattern1, InfoPattern pattern2) {
        if (pattern1 == null)
            return pattern2;

        if (pattern2 == null)
            return pattern1;

        LinkedHashMap<String, SerializationType<?>> field2type = new LinkedHashMap<>();
        LinkedHashMap<String, String> field2title = new LinkedHashMap<>();

        field2type.putAll(pattern1.field2type);
        field2type.putAll(pattern2.field2type);

        field2title.putAll(pattern1.field2title);
        field2title.putAll(pattern2.field2title);

        return new InfoPattern(field2type, field2title);
    }

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

    public SerializationType getType(String field) {
        return field2type.get(field);
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

    public String toJSON(Info value) {
        JSONSerializer serializer = new JSONSerializer();
        write(value, serializer);
        return serializer.getNode().toString();
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

    public InputForm getInputForm() {
        List<InputField> fieldsList = new ArrayList<>();

        List<Validator> emptyImmutableValidatorsList = Collections.emptyList();

        for (String field : field2type.keySet()) {
            SerializationType<?> type = field2type.get(field);
            String title = field2title.get(field);
            fieldsList.add(new InputField(field, getInputFieldByType(type, title), false, emptyImmutableValidatorsList));
        }

        return new InputForm(fieldsList, emptyImmutableValidatorsList);
    }

    private InputTemplate getInputFieldByType(SerializationType<?> type, String title) {
        if (!(type instanceof BasicSerializationType))
            throw new IllegalArgumentException("failed to get editor for type");

        String className = ((BasicSerializationType) type).getClassName();

        switch (className) {
            case "string":
                return InputTemplate.get("string", "title", title, "placeholder", title);
            case "int":
                return InputTemplate.get("int", "title", title, "placeholder", title);
            case "boolean":
                return InputTemplate.get("boolean", "title");
        }

        throw new IllegalArgumentException("failed to get editor for type");
    }

    public Html simpleFormat(Info info) {
        StringBuilder result = new StringBuilder();

        for (String field : field2title.keySet()) {
            Object value = info.get(field);
            if (result.length() > 0)
                result.append(", ");
            result
                    .append(views.html.htmlfeatures.string2html.render(field2title.get(field)))
                    .append(": ").append("<b>")
                    .append(value == null ? "-" : views.html.htmlfeatures.string2html.render(value.toString()))
                    .append("</b>");
        }

        return Html.apply(result.toString());
    }
}
