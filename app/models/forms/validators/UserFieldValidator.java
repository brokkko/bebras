package models.forms.validators;

import models.User;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 16:32
 */
public class UserFieldValidator extends Validator {
    //TODO make the same for other fields

    public UserFieldValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
    }

    @Override
    public String validate(Object value) {
        String login = (String)value;

        User user = User.getInstance((String) validationParameters.get("field"), login);

        return user == null ? null : message();
    }
}