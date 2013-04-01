package models.newmodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 14:37
 */
public class InputForm {

    private String name;
    private LinkedHashMap<String, InputField> fields;
    private List<Validator> validators;

    public InputForm(String name, LinkedHashMap<String, InputField> fields, List<Validator> validators) {
        this.name = name;
        this.fields = fields;
        this.validators = validators;
    }

    public String getName() {
        return name;
    }

    /*
    @Override
    public void store(Serializer serializer) {
        Serializer fieldsSerializer = serializer.getSerializer("fields");
        for (Map.Entry<String, InputField> fieldEntry : fields.entrySet()) {
            Serializer fieldSerializer = fieldsSerializer.getSerializer(fieldEntry.getKey());
            fieldEntry.getValue().store(fieldSerializer);
        }

        ListSerializer validatorsSerializer = serializer.getListSerializer("validators");
        for (Validator validator : validators) {
            Serializer validatorSerializer = validatorsSerializer.getSerializer();
            validator.store(validatorSerializer);
        }
    }
    */

    public static InputForm deserialize(String name, Deserializer deserializer) {
        LinkedHashMap<String, InputField> fields = new LinkedHashMap<>();

        Deserializer fieldsDeserializer = deserializer.getDeserializer("fields");
        for (String fieldName : fieldsDeserializer.fieldSet()) {
            Deserializer inputFieldDeserializer = fieldsDeserializer.getDeserializer(fieldName);
            fields.put(fieldName, InputField.deserialize(inputFieldDeserializer));
        }

        List<Validator> validators = new ArrayList<>();

        ListDeserializer validatorsList = deserializer.getListDeserializer("validators");
        while (validatorsList.hasMore()) {
            Validator validator = Validator.deserialize(validatorsList.getDeserializer());
            validators.add(validator);
        }

        return new InputForm(name, fields, validators);
    }
}
