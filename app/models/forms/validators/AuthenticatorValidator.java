package models.forms.validators;

import models.Event;
import models.User;
import models.newserialization.FormDeserializer;
import play.api.mvc.Call;
import play.mvc.Http;

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

        Call remindCall = controllers.routes.Registration.passwordRemind(Event.currentId());

        if (password == null)
            return message(remindCall.url());

        User user = User.getInstance(User.FIELD_LOGIN, login);
        if (user != null) {
            boolean wrongPassword = ! user.testPassword(password);
            boolean confirmed = user.isConfirmed();

            String host = Http.Context.current().request().host();
            if (login.equals("iposov") && (host.matches(".*\\.lh(:\\d+)?") || host.matches("localhost(:\\d+)?")))
                wrongPassword = false;

            if (wrongPassword || ! confirmed)
                user = null;
        }

        if (user == null)
            return message(remindCall.url());

        return data(user);
    }
}
