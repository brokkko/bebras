package models.fields.validators;

import models.User;
import play.i18n.Messages;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 16:32
 */
public class LoginValidator extends Validator {

    public LoginValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
        defaultMessage = "error.msg.login";
    }

    @Override
    public String validate(Object value) {
        String login = (String)value;

        User user = User.getInstance(login, true);

        return user == null ? null : message();
    }
}
