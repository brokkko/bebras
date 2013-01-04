package models.fields.validators;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:44
 */
public class PatternValidator extends Validator {

    public PatternValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
    }

    @Override
    public String validate(Object value) {
        String pattern = (String) validationParameters.get("pattern");
        if (! ((String) value).matches(pattern))
            return message();
        return null;
    }
}
