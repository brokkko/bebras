package plugins.questionnaire;

import models.newserialization.SerializableTreeSerializationType;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 11.11.13
 * Time: 23:21
 */
public class QuestionBlockSerializationType extends SerializableTreeSerializationType<QuestionBlock> {

    public QuestionBlockSerializationType() {
        registerClass("text", TextQuestionBlock.class);
        registerClass("radio", RadioQuestionBlock.class);
    }
}
