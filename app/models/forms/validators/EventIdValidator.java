package models.forms.validators;

import models.Event;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.08.13
 * Time: 20:00
 */
public class EventIdValidator extends Validator<String> {
    @Override
    public Validator.ValidationResult validate(String value) {
        return Event.getInstance(value) == null ? ok() : message();
    }
}
