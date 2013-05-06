package models.serialization;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.05.13
 * Time: 12:17
 */
public class JSONSerializer implements Serializer {

    private ObjectNode node = JsonNodeFactory.instance.objectNode();

    public ObjectNode getNode() {
        return node;
    }

    @Override
    public void write(String field, Object value) {
        if (value == null)
            node.put(field, (String) null);
        else if (value instanceof JsonNode)
            node.put(field, (JsonNode) value);
        else if (value instanceof Integer)
            node.put(field, (Integer) value);
        else if (value instanceof String)
            node.put(field, (String) value);
        else if (value instanceof Long)
            node.put(field, (Long) value);
        else if (value instanceof Float)
            node.put(field, (Float) value);
        else if (value instanceof Double)
            node.put(field, (Double) value);
        else if (value instanceof Boolean)
            node.put(field, (Boolean) value);
        else
            node.put(field, String.valueOf(value));
    }

    @Override
    public Serializer getSerializer(String field) {
        JSONSerializer serializer = new JSONSerializer();
        write(field, serializer.getNode());
        return serializer;
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        JSONListSerializer serializer = new JSONListSerializer();
        write(field, serializer.getNode());
        return serializer;
    }
}
