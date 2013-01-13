package models.forms.validators;

import models.User;
import models.forms.InputForm;
import play.data.DynamicForm;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 13.01.13
 * Time: 4:06
 */
public class AuthenticatorValidator extends Validator {
    public AuthenticatorValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
        defaultMessage = "error.msg.failed_to_authenticate";
    }

    @Override
    public String validate(Object value) {
        InputForm.FilledInputForm form = (InputForm.FilledInputForm) value;

        String login = (String) form.get("login"); //they are required, so we sure that we can get this info
        String password = (String) form.get("password");

        User user = User.getInstance(User.FIELD_LOGIN, login);
        if (user != null && !user.testPassword(password))
            user = null;

        if (user == null)
            return message();

        return null;
    }
}
