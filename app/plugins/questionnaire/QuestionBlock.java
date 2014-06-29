package plugins.questionnaire;

import models.newserialization.SerializableUpdatable;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 11.11.13
 * Time: 18:47
 */
public interface QuestionBlock extends SerializableUpdatable {

    String getName(); //may return null
    Html render(Object value);

}
