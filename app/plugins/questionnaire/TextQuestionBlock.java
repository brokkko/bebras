package plugins.questionnaire;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.twirl.api.Html;
//import play.twirl.api.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 11.11.13
 * Time: 23:42
 */
public class TextQuestionBlock implements QuestionBlock {

    private String text;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Html render(Object value) {
        return Html.apply(text);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("text", text);
    }

    @Override
    public void update(Deserializer deserializer) {
        text = deserializer.readString("text");
    }
}
