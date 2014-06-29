package models.forms.validators;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;
import play.i18n.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 23:42
 */
public abstract class Validator<T> implements SerializableUpdatable {

    private static final ValidationResult OK = new ValidationResult(null, null);

    protected String defaultMessage;
    private String message;

    protected ValidationResult custom(String text) {
        return new ValidationResult(Messages.get(text), null);
    }

    protected ValidationResult message(Object... args) {
        String result = message;
        if (result == null)
            result = defaultMessage;
        if (result == null)
            throw new IllegalArgumentException("No message specified for the validator " + this.getClass().getCanonicalName());

        return new ValidationResult(Messages.get(result, args), null);
    }

    protected ValidationResult ok() {
        return OK;
    }

    protected ValidationResult data(Object validationData) {
        return new ValidationResult(null, validationData);
    }

    public abstract ValidationResult validate(T value);

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("message", message);
    }

    @Override
    public void update(Deserializer deserializer) {
        message = deserializer.readString("message");
    }

    public static class ValidationResult {
        private String message;
        private Object validationData;

        private ValidationResult(String message, Object validationData) {
            this.message = message;
            this.validationData = validationData;
        }

        public String getMessage() {
            return message;
        }

        public Object getValidationData() {
            return validationData;
        }
    }
}
