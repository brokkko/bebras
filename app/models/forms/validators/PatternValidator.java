package models.forms.validators;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:44
 */
public class PatternValidator extends Validator<String> {

    private String pattern;

    @Override
    public Validator.ValidationResult validate(String value) {
        if (! value.matches(pattern))
            return message();
        return ok();
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        pattern = deserializer.readString("pattern");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("pattern", pattern);
    }
}
