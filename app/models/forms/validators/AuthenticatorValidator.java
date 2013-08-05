package models.forms.validators;

import models.User;
import models.newserialization.FormDeserializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 13.01.13
 * Time: 4:06
 */
public class AuthenticatorValidator extends Validator<FormDeserializer> {

    public AuthenticatorValidator() {
        defaultMessage = "error.msg.failed_to_authenticate";
    }

    @Override
    public Validator.ValidationResult validate(FormDeserializer form) {

        String login = form.readString("login"); //they are required, so we sure that we can get this info
        String password = form.readString("password");

        if (password == null)
            return message();

        User user = User.getInstance(User.FIELD_LOGIN, login);
        if (user != null) {
            boolean wrongPassword = ! user.testPassword(password);
            Boolean confirmed = user.isConfirmed();

            if (wrongPassword || confirmed == null || ! confirmed)
                user = null;
        }

        if (user == null)
            return message();

        return data(user);
    }
}
