package models.forms.validators;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.01.13
 * Time: 19:23
 */
public class PhoneValidator extends Validator<String> {

    public PhoneValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
        defaultMessage = "error.msg.phone";
    }

    @Override
    public String validate(String phone) {
        //filter out spaces and -
        phone = phone.replaceAll("[- ]", "");

        //replace +7 with 8 in the beginning
        if (phone.matches("\\+\\d.*"))
            phone = "8" + phone.substring(2);

        //forbid --
        if (phone.contains("--"))
            return message();

        //remove pair of brackets if there is one
        if (phone.matches("[^\\(\\)]*\\([^\\(\\)]*\\)[^\\(\\)]*"))
            phone = phone.replaceAll("[\\(\\)]", "");

        if (! phone.matches("\\d{5,12}"))
            return message();

        return null;
    }
}
