package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newserialization.Deserializer;
import models.newserialization.SerializationType;
import models.newserialization.Serializer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import play.api.templates.Html;
import play.i18n.Messages;
import views.html.fields.multiline;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.08.13
 * Time: 16:16
 */
public class JsonListInputTemplate extends InputTemplate<ArrayNode> {
    //TODO this is almost code duplication with JsonInputTemplate

    private String placeholder;

    @Override
    public Html render(RawForm form, String field) {
        return multiline.render(form, field, placeholder, false);
    }

    @Override
    public void write(String field, final ArrayNode value, RawForm rawForm) {
        rawForm.put(field, new Object() {
            @Override
            public String toString() {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
                    return writer.writeValueAsString(value);

                    /*StringWriter out = new StringWriter();
                    JsonGenerator jg = new JsonFactory().createJsonGenerator(out);
                    jg.setPrettyPrinter(new DefaultPrettyPrinter());
                    jg.writeObject(value);
                    return out.toString();*/
                } catch (IOException e) {
                    return ""; //can not occur
                }
            }
        });
    }

    @Override
    public ArrayNode read(String field, RawForm form) {
        String json = form.get(field);
        if (json == null)
            return null;
        try {
            //http://stackoverflow.com/questions/3653996/how-to-parse-a-json-string-into-jsonnode-in-jackson
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory f = mapper.getJsonFactory();
            JsonParser parser = f.createJsonParser(json);
            JsonNode tree = mapper.readTree(parser);
            if (tree instanceof ArrayNode)
                return (ArrayNode) tree;
            else {
                form.reject(field, Messages.get("error.msg.json_object"));
                return null;
            }
        } catch (IOException e) {
            form.reject(field, Messages.get("error.msg.json_object") + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public SerializationType<ArrayNode> getType() {
        return null; //TODO implement this serialization type
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        placeholder = deserializer.readString("placeholder", "");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("placeholder", placeholder);
    }
}
