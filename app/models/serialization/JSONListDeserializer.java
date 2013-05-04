package models.serialization;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.05.13
 * Time: 12:14
 */
public class JSONListDeserializer implements ListDeserializer {
    private ArrayNode node;
    private int index = 0;

    public JSONListDeserializer(ArrayNode node) {
        this.node = node;
    }

    @Override
    public boolean hasMore() {
        return index < node.size();
    }

    @Override
    public int getInt() {
        return (Integer) getObject();
    }

    @Override
    public boolean getBoolean() {
        return (Boolean) getObject();
    }

    @Override
    public String getString() {
        return (String) getObject();
    }

    @Override
    public Object getObject() {
        return node.get(index++);
    }

    @Override
    public Deserializer getDeserializer() {
        return new JSONDeserializer((ObjectNode) getObject());
    }

    @Override
    public ListDeserializer getListDeserializer() {
        return new JSONListDeserializer((ArrayNode) getObject());
    }
}
