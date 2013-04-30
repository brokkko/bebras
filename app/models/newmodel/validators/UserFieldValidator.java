package models.newmodel.validators;

import models.User;
import models.newmodel.Deserializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 16:32
 */
public class UserFieldValidator extends Validator<String> {

    private final String field;

    public UserFieldValidator(Deserializer deserializer) {
        field = deserializer.getString("field");
    }

    @Override
    public String validate(String login) {
        User user = User.getInstance(field, login);

        return user == null ? null : getMessage();
    }
}
