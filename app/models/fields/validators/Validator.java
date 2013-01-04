package models.fields.validators;

import play.data.validation.Constraints;
import play.i18n.Messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 21:33
 */
public abstract class Validator {

    protected String defaultMessage;

    @Constraints.Email
    protected final Map<String, Object> validationParameters;

    protected Validator(Map<String, Object> validationParameters) {
        this.validationParameters = validationParameters;
    }

    protected String message(Object... args) {
        String message = (String) validationParameters.get("message");
        if (message == null)
            message = defaultMessage;
        if (message == null)
            throw new IllegalArgumentException("No message specified for the validator " + this.getClass().getCanonicalName());
        return Messages.get(message, args);
    }

    public static Validator getInstance(String type, Map<String, Object> validationParameters) {
        String className = Validator.class.getPackage().getName() + "." + capitalize(type) + "Validator";

        try {
            Class<?> probableValidatorClass = Class.forName(className);
            Class<? extends Validator> validatorClass = probableValidatorClass.asSubclass(Validator.class);
            Constructor<? extends Validator> constructor = validatorClass.getConstructor(Map.class);
            return constructor.newInstance(validationParameters);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unknown input validator '" + type + "'");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Validator '" + type + "' does not have an appropriate constructor");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Validator '" + type + "' threw an exception");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Validator '" + type + "' can not be instantiated");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Validator '" + type + "' has constructor that is not public");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Validator '" + type + "' is not a subclass of " + Validator.class.getCanonicalName());
        }
    }

    private static String capitalize(String type) {
        return type.substring(0, 1).toUpperCase() + type.substring(1);
    }

    public abstract String validate(Object value);
}
