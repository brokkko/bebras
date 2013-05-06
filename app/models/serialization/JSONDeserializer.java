package models.serialization;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.05.13
 * Time: 12:04
 */
public class JSONDeserializer implements Deserializer {

    private ObjectNode node;

    public JSONDeserializer(ObjectNode node) {
        this.node = node;
    }

    @Override
    public int getInt(String field) {
        return (Integer) getObject(field);
    }

    @Override
    public Boolean getBoolean(String field) {
        return (Boolean) getObject(field);
    }

    @Override
    public String getString(String field) {
        return (String) getObject(field);
    }

    @Override
    public Object getObject(String field) {
        JsonNode jsonNode = node.get(field);

        if (jsonNode == null)
            return null;

        if (jsonNode.isInt())
            return jsonNode.getIntValue();
        if (jsonNode.isBoolean())
            return jsonNode.getBooleanValue();
        if (jsonNode.isTextual())
            return jsonNode.getTextValue();

        if (jsonNode.isLong())
            return jsonNode.getLongValue();
        //TODO add other value types that are may be used in a program

        return jsonNode;
    }

    @Override
    public Deserializer getDeserializer(String field) {
        return new JSONDeserializer((ObjectNode) getObject(field));
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        return new JSONListDeserializer((ArrayNode) getObject(field));
    }

    @Override
    public Set<String> fieldSet() {
        Set<String> fields = new HashSet<>();

        Iterator<String> it = node.getFieldNames();
        while (it.hasNext())
            fields.add(it.next());

        return fields;
    }
}
