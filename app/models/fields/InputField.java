package models.fields;

import models.StoredObject;
import play.api.templates.Html;
import play.data.DynamicForm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.12.12
 * Time: 1:52
 */
public class InputField {

    private final String name;

    private final InputTemplate inputTemplate;
    private final Map<String, Object> inputConfiguration;

    private final List<InputValidator> validators;

    public InputField(String name, InputTemplate inputTemplate, Map<String, Object> inputConfiguration, List<InputValidator> validators) {
        this.name = name;
        this.inputTemplate = inputTemplate;
        this.inputConfiguration = inputConfiguration;
        this.validators = validators;
    }

    public InputField(InputTemplate inputTemplate, String name, Map<String, Object> inputConfiguration, InputValidator... validators) {
        this.name = name;
        this.inputTemplate = inputTemplate;
        this.inputConfiguration = inputConfiguration;
        this.validators = Arrays.asList(validators);
    }

    public InputField(StoredObject storedObject) {
        this.name = storedObject.getString("name");

        //get input method
        StoredObject inputConfig = storedObject.getObject("input");
        this.inputTemplate = InputTemplate.getInstance(inputConfig.getString("type"));

        //get input template arguments
        this.inputConfiguration = inputConfig.toMap();

        //get validators
        ArrayList<InputValidator> validators = new ArrayList<>();

        List validatorsConfig = storedObject.getList("validators");
        if (validatorsConfig != null)
            for (Object validator : validatorsConfig) {
                StoredObject validatorConfig = (StoredObject) validator;
                validators.add(InputValidator.getInstance(validatorConfig.getString("type"), validatorConfig.toMap()));
            }

        this.validators = validators;
    }

    public Html format(DynamicForm form) {
        return inputTemplate.format(form, name, inputConfiguration);
    }

    public void validate(DynamicForm form) {
        for (InputValidator validator : validators)
            validator.validate(form.get(name));
    }

}
