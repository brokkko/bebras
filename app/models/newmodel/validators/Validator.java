package models.newmodel.validators;

import models.newmodel.Deserializer;
import play.i18n.Messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 23:42
 */
public abstract class Validator<T> {

    protected String defaultMessage;
    private String message;

    public static Validator deserialize(Deserializer deserializer) {
        String type = deserializer.getString("type");

        String className = Validator.class.getPackage().getName() + "." + capitalize(type) + "Validator";

        try {
            Class<?> probableValidatorClass = Class.forName(className);
            Class<? extends Validator> validatorClass = probableValidatorClass.asSubclass(Validator.class);
            Constructor<? extends Validator> constructor = validatorClass.getConstructor(Deserializer.class);

            String message = deserializer.getString("message");

            Validator validator = constructor.newInstance(deserializer);
            validator.setMessage(message);
            return validator;
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

    protected void setMessage(String message) {
        this.message = message;
    }

    protected String getMessage(Object... args) {
        String result = message;
        if (result == null)
            result = defaultMessage;
        if (result == null)
            throw new IllegalArgumentException("No message specified for the validator " + this.getClass().getCanonicalName());
        return Messages.get(result, args);
    }

    public abstract String validate(T value);

}
