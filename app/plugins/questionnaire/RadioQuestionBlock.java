package plugins.questionnaire;

import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import play.api.templates.Html;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 11.11.13
 * Time: 23:45
 */
public class RadioQuestionBlock implements QuestionBlock {

    private String name;
    private List<String> options;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Html render(Object value) {
        return views.html.questionnaire.radio_block.render(name, (String) value, options);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        SerializationTypesRegistry.list(String.class).write(serializer, "options", options);
    }

    @Override
    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        options = SerializationTypesRegistry.list(String.class).read(deserializer, "options");
    }
}
