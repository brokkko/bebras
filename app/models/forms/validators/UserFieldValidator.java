package models.forms.validators;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 16:32
 */
public class UserFieldValidator extends Validator<String> {

    private String field;

    @Override
    public Validator.ValidationResult validate(String value) {
        User user = User.getInstance(field, value);

        return user == null ? ok() : message();
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        field = deserializer.readString("field");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("field", field);
    }
}
