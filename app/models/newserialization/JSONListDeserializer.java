package models.newserialization;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Created by ilya
 */
public class JSONListDeserializer extends ListDeserializer {
    private ArrayNode node;
    private int index = 0;

    public JSONListDeserializer(ArrayNode node) {
        this.node = node;
    }

    @Override
    public Integer readInt() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.getIntValue();
    }

    @Override
    public Long readLong() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.getLongValue();
    }

    @Override
    public Double readDouble() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.getDoubleValue();
    }

    @Override
    public Boolean readBoolean() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.getBooleanValue();
    }

    @Override
    public String readString() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.getTextValue();
    }

    @Override
    public Deserializer getDeserializer() {
        ObjectNode subNode = (ObjectNode) node.get(index++);
        return subNode == null ? null : new JSONDeserializer(subNode);
    }

    @Override
    public ListDeserializer getListDeserializer() {
        ArrayNode subNode = (ArrayNode) node.get(index++);
        return subNode == null ? null : new JSONListDeserializer(subNode);
    }

    @Override
    public boolean hasMore() {
        return index < node.size();
    }

    @Override
    public boolean nextIsNull() {
        return node.get(index).isNull();
    }
}
