package models.newmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 22:49
 */
public class InputField {

    private String name;
    private InputTemplate inputTemplate;
    private Map<String, Object> additionalConfiguration;
    private List<Validator> validators;

    public InputField(String name, InputTemplate inputTemplate, Map<String, Object> additionalConfiguration, List<Validator> validators) {
        this.name = name;
        this.inputTemplate = inputTemplate;
        this.additionalConfiguration = additionalConfiguration;
        this.validators = validators;
    }

    public String getName() {
        return name;
    }

    public static InputField deserialize(Deserializer deserializer) {
        String name = null;
        InputTemplate inputTemplate = null;
        Map<String, Object> additionalConfiguration = new HashMap<>();
        List<Validator> validators = new ArrayList<>();

        for (String fieldName : deserializer.fieldSet()) {
            switch (fieldName) {
                case "name":
                    name = deserializer.getString(fieldName);
                    break;
                case "type":
                    inputTemplate = InputTemplate.getInstance(deserializer.getString(fieldName));
                    break;
                case "validators":
                    ListDeserializer validatorsDeserializer = deserializer.getListDeserializer(fieldName);
                    while (validatorsDeserializer.hasMore()) {
                        Deserializer validatorDeserializer = validatorsDeserializer.getDeserializer();
                        validators.add(Validator.deserialize(validatorDeserializer));
                    }
                    break;
                default:
                    additionalConfiguration.put(fieldName, deserializer.getObject(fieldName));
            }
        }

        return new InputField(name, inputTemplate, additionalConfiguration, validators);
    }
}
