package models.forms.validators;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 26.07.13
 * Time: 0:26
 */
public class BooleanValidator extends Validator<Boolean> {

    private boolean neededValue;

    @Override
    public Validator.ValidationResult validate(Boolean value) {
        if (value == null || value != neededValue)
            return message();
        return ok();
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        neededValue = deserializer.readBoolean("need", true);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("need", neededValue);
    }
}
