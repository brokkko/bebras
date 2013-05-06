package models.forms.validators;

import models.serialization.Deserializer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:57
 */
public class EmailValidator extends Validator<String> {
    public EmailValidator(Deserializer deserializer) {
        defaultMessage = "error.msg.email";
    }

    @Override
    public String validate(String email) {
        if (email.indexOf('@') < 0)
            return getMessage();

        try {
            new InternetAddress(email);
        } catch (AddressException e) {
            return getMessage();
        }
        return null;
    }
}
