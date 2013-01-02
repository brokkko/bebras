package models.fields;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 21:33
 */
public abstract class InputValidator {

    protected Map<String, Object> validationParameters;

    protected InputValidator(Map<String, Object> validationParameters) {
        this.validationParameters = validationParameters;
    }

    public static InputValidator getInstance(String type, Map<String, Object> validationParameters) {
        switch (type) {
            case "pattern":
                return new PatternValidator(validationParameters);
        }
        throw new IllegalArgumentException("Unknown input validator '" + type + "'");
    }

    public abstract String validate(Object value);
}
