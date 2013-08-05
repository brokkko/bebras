package models.forms.validators;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:57
 */
public class CodeWordValidator extends Validator<String> {

    private String answer;

    public CodeWordValidator() {
        defaultMessage = "error.msg.code_word";
    }

    @Override
    public Validator.ValidationResult validate(String codeWord) {
        return codeWord.equals(answer) ? ok() : message();
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        answer = deserializer.readString("answer");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("answer", answer);
    }
}
