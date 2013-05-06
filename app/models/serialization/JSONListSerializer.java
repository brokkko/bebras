package models.serialization;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.05.13
 * Time: 12:24
 */
public class JSONListSerializer implements ListSerializer {

    private ArrayNode node = JsonNodeFactory.instance.arrayNode();

    public ArrayNode getNode() {
        return node;
    }

    @Override
    public void write(Object value) {
        if (value == null)
            node.add((String)null);
        if (value instanceof JsonNode)
            node.add((JsonNode) value);
        else if (value instanceof Integer)
            node.add((Integer) value);
        else if (value instanceof String)
            node.add((String) value);
        else if (value instanceof Long)
            node.add((Long) value);
        else if (value instanceof Float)
            node.add((Float) value);
        else if (value instanceof Double)
            node.add((Double) value);
        else if (value instanceof Boolean)
            node.add((Boolean) value);
        else
            node.add(String.valueOf(value));
    }

    @Override
    public Serializer getSerializer() {
        JSONSerializer serializer = new JSONSerializer();
        write(serializer.getNode());
        return serializer;
    }

    @Override
    public ListSerializer getListSerializer() {
        JSONListSerializer serializer = new JSONListSerializer();
        write(serializer.getNode());
        return serializer;
    }
}
