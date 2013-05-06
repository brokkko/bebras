package models.forms.validators;

import models.serialization.Deserializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:57
 */
public class CodeWordValidator extends Validator<String> {

    private final String answer;

    public CodeWordValidator(Deserializer deserializer) {
        defaultMessage = "error.msg.code_word";

        answer = deserializer.getString("answer");
    }

    @Override
    public String validate(String codeWord) {
        return codeWord.equals(answer) ? null : getMessage();
    }
}
