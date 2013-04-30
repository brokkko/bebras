package models.forms.validators;

import models.serialization.Deserializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:44
 */
public class PatternValidator extends Validator<String> {

    private final String pattern;

    public PatternValidator(Deserializer deserializer) {
        pattern = deserializer.getString("pattern");
    }

    @Override
    public String validate(String value) {
        if (! value.matches(pattern))
            return getMessage();
        return null;
    }
}
