package models.newserialization;

import models.utils.Utils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

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
        return sub == null ? null : sub.getIntValue();
    }

    @Override
    public Long readLong(String field) {
        JsonNode sub = node.get(field);
        return sub == null ? null : sub.getLongValue();
    }

    @Override
    public Double readDouble(String field) {
        JsonNode sub = node.get(field);
        return sub == null ? null : sub.getDoubleValue();
    }

    @Override
    public Boolean readBoolean(String field) {
        JsonNode sub = node.get(field);
        return sub == null ? null : sub.getBooleanValue();
    }

    @Override
    public String readString(String field) {
        JsonNode sub = node.get(field);
        return sub == null ? null : sub.getTextValue();
    }

    @Override
    public Deserializer getDeserializer(String field) {
        ObjectNode subNode = (ObjectNode) node.get(field);
        return subNode == null ? null : new JSONDeserializer(subNode);
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        ArrayNode subNode = (ArrayNode) node.get(field);
        return subNode == null ? null : new JSONListDeserializer(subNode);
    }

    @Override
    public Collection<String> fields() {
        Set<String> fields = new HashSet<>();

        Iterator<String> it = node.getFieldNames();
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