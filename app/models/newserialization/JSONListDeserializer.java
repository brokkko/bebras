package models.newserialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
        return sub == null ? null : sub.asInt();
    }

    @Override
    public Long readLong() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.asLong();
    }

    @Override
    public Double readDouble() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.asDouble();
    }

    @Override
    public Boolean readBoolean() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.asBoolean();
    }

    @Override
    public String readString() {
        JsonNode sub = node.get(index++);
        return sub == null ? null : sub.asText();
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
