package models.newserialization;

import models.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Created by ilya
 */
public class JSONDeserializer extends Deserializer {

    private ObjectNode node;

    public JSONDeserializer(ObjectNode node) {
        this.node = node;
    }

    @Override
    public Integer readInt(String field) {
        JsonNode sub = node.get(field);
        return sub == null || sub.isNull() ? null : sub.asInt();
    }

    @Override
    public Long readLong(String field) {
        JsonNode sub = node.get(field);
        return sub == null || sub.isNull() ? null : sub.asLong();
    }

    @Override
    public Double readDouble(String field) {
        JsonNode sub = node.get(field);
        return sub == null || sub.isNull() ? null : sub.asDouble();
    }

    @Override
    public Boolean readBoolean(String field) {
        JsonNode sub = node.get(field);
        return sub == null || sub.isNull() ? null : sub.asBoolean();
    }

    @Override
    public String readString(String field) {
        JsonNode sub = node.get(field);
        return sub == null || sub.isNull() ? null : sub.asText();
    }

    @Override
    public Deserializer getDeserializer(String field) {
        ObjectNode subNode = (ObjectNode) node.get(field);
        return subNode == null || subNode.isNull() ? null : new JSONDeserializer(subNode);
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        ArrayNode subNode = (ArrayNode) node.get(field);
        return subNode == null || subNode.isNull() ? null : new JSONListDeserializer(subNode);
    }

    @Override
    public Collection<String> fields() {
        Set<String> fields = new HashSet<>();

        Iterator<String> it = node.fieldNames();
        while (it.hasNext())
            fields.add(it.next());

        return fields;
    }

    @Override
    public boolean isNull(String field) {
        return node.get(field) == null || node.get(field).isNull();
    }

    @Override
    public Date readDate(String field) {
        try {
            return super.readDate(field);    //To change body of overridden methods use File | Settings | File Templates.
        } catch (NumberFormatException ignored) {
            return Utils.parseSimpleTime(readString(field));
        }
    }
}