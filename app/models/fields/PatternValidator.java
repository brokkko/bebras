package models.fields;

import play.data.validation.Constraints;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:44
 */
public class PatternValidator extends InputValidator {

    protected PatternValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
    }

    @Override
    public String validate(Object value) {
        String pattern = (String) validationParameters.get("pattern");
        if (! ((String) value).matches(pattern))
            return ((String) validationParameters.get("message"));
        return null;
    }
}
