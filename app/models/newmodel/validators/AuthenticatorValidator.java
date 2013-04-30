package models.newmodel.validators;

import models.User;
import models.newmodel.Deserializer;
import models.newmodel.FormDeserializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 13.01.13
 * Time: 4:06
 */
public class AuthenticatorValidator extends Validator<FormDeserializer> {

    public static String VALIDATED_USER = "user";

    public AuthenticatorValidator(Deserializer deserializer) {
        defaultMessage = "error.msg.failed_to_authenticate";
    }

    @Override
    public String validate(FormDeserializer form) {

        String login = form.getString("login"); //they are required, so we sure that we can get this info
        String password = form.getString("password");

        User user = User.getInstance(User.FIELD_LOGIN, login);
        if (user != null) {
            boolean wrongPassword = ! user.testPassword(password);
            Boolean confirmed = (Boolean) user.get(User.FIELD_CONFIRMED);

            if (wrongPassword || confirmed == null || ! confirmed)
                user = null;
        }

        if (user == null)
            return getMessage();

        form.putValidationData(VALIDATED_USER, user);

        return null;
    }
}
