package models.newserialization;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by ilya
 */
public class JSONSerializer extends Serializer {

    private final ObjectNode node = JsonNodeFactory.instance.objectNode();

    public ObjectNode getNode() {
        return node;
    }

    @Override
    public void write(String field, int value) {
        node.put(field, value);
    }

    @Override
    public void write(String field, long value) {
        node.put(field, value);
    }

    @Override
    public void write(String field, double value) {
        node.put(field, value);
    }

    @Override
    public void write(String field, boolean value) {
        node.put(field, value);
    }

    @Override
    public void write(String field, String value) {
        node.put(field, value);
    }

    @Override
    public Serializer getSerializer(String field) {
        JSONSerializer serializer = new JSONSerializer();
        node.put(field, serializer.getNode());
        return serializer;
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        JSONListSerializer serializer = new JSONListSerializer();
        node.put(field, serializer.getNode());
        return serializer;
    }
}
