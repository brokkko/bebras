package models.forms.validators;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.07.13
 * Time: 17:49
 */
public class IntegerValidator extends Validator<Integer> {

    private String compare = null;

    @Override
    public Validator.ValidationResult validate(Integer value) {
        if (compare == null)
            return ok();

        switch (compare) {
            case ">0":
                return value > 0 ? ok() : custom("Число должно быть больше нуля");
            case ">=0":
                return value >= 0 ? ok() : custom("Число должно быть неотрицательным");
            case "<0":
                return value < 0 ? ok() : custom("Число должно быть меньше нуля");
            case "<=0":
                return value <= 0 ? ok() : custom("Число должно быть неположительным");
        }

        return custom("Неизвестный формат сравнения");
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        compare = deserializer.readString("compare");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("compare", compare);
    }
}
