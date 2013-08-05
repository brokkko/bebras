package models.forms.validators;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:57
 */
public class EmailValidator extends Validator<String> {

    public EmailValidator() {
        defaultMessage = "error.msg.email";
    }

    @Override
    public Validator.ValidationResult validate(String email) {
        if (email.indexOf('@') < 0)
            return message();

        try {
            new InternetAddress(email);
        } catch (AddressException e) {
            return message();
        }
        return ok();
    }
}
