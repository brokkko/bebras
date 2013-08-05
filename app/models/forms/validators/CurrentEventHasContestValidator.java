package models.forms.validators;

import models.Event;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 03.08.13
 * Time: 21:53
 */
public class CurrentEventHasContestValidator extends Validator<String> {

    public CurrentEventHasContestValidator() {
        defaultMessage = "error.msg.event_has_contest";
    }

    @Override

    public Validator.ValidationResult validate(String value) {
        Event event = Event.current();
        if (event.getContestById(value) != null)
            return message();
        return ok();
    }
}
