package models.newserialization;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;

/**
 * Created by ilya
 */
public class JSONListSerializer extends ListSerializer {

    private ArrayNode node = JsonNodeFactory.instance.arrayNode();

    public ArrayNode getNode() {
        return node;
    }

    @Override
    public void write(int value) {
        node.add(value);
    }

    @Override
    public void write(long value) {
        node.add(value);

    }

    @Override
    public void write(double value) {
        node.add(value);
    }

    @Override
    public void write(boolean value) {
        node.add(value);
    }

    @Override
    public void write(String value) {
        node.add(value);
    }

    @Override
    public Serializer getSerializer() {
        JSONSerializer serializer = new JSONSerializer();
        node.add(serializer.getNode());
        return serializer;
    }

    @Override
    public ListSerializer getListSerializer() {
        JSONListSerializer serializer = new JSONListSerializer();
        node.add(serializer.getNode());
        return serializer;
    }
}
