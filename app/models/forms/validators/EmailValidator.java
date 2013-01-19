package models.forms.validators;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:57
 */
public class EmailValidator extends Validator<String> {
    public EmailValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
        defaultMessage = "error.msg.email";
    }

    @Override
    public String validate(String email) {
        if (email.indexOf('@') < 0)
            return message();

        try {
            new InternetAddress(email);
        } catch (AddressException e) {
            return message();
        }
        return null;
    }
}
