package models.forms;

import models.StoredObject;
import models.forms.inputtemplate.InputTemplate;
import models.forms.inputtemplate.InputTemplateConfig;
import models.forms.validators.Validator;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.12.12
 * Time: 1:52
 */
public class InputField {

    private final InputForm form;
    private final String name;

    private final InputTemplate inputTemplate;
    private final InputTemplateConfig inputConfiguration;

    private final List<Validator> validators;

    public InputForm getForm() {
        return form;
    }

    public String getName() {
        return name;
    }

    public InputField(InputForm form, StoredObject storedObject) {
        this.form = form;
        this.name = storedObject.getString("name");

        //get input method
        StoredObject inputConfig = storedObject.getObject("input");
        this.inputTemplate = InputTemplate.getInstance(inputConfig.getString("type"));

        //get input template arguments
        this.inputConfiguration = new InputTemplateConfig(inputConfig.toMap(), this);

        //get validators
        ArrayList<Validator> validators = new ArrayList<>();

        List validatorsConfig = inputConfig.getList("validators");
        if (validatorsConfig != null)
            for (Object validator : validatorsConfig) {
                StoredObject validatorConfig = (StoredObject) validator;
                validators.add(Validator.getInstance(validatorConfig.getString("type"), validatorConfig.toMap()));
            }
        else {
            StoredObject validatorConfig = inputConfig.getObject("validator");
            if (validatorConfig != null)
                //TODO this is a small code duplication
                validators.add(Validator.getInstance(validatorConfig.getString("type"), validatorConfig.toMap()));
        }

        this.validators = validators;
    }

    public Html format(DynamicForm form) {
        return inputTemplate.format(form, name, inputConfiguration);
    }

    public void validate(DynamicForm form) {
        InputTemplate.BindResult bindResult = inputTemplate.getObject(form, name);
        if (bindResult.hasErrors()) {
            for (String message : bindResult.getMessages())
                form.reject("data[" + name + "]", message); //TODO why should I write data[] here?

            return;
        }

        Object value = bindResult.getValue();

        if (inputConfiguration.isRequired() && value == null) {
            form.reject("data[" + name + "]", Messages.get("error.msg.required"));
            return;
        }

        for (Validator validator : validators) {
            String message = validator.validate(value);
            if (message != null) {
                form.reject("data[" + name + "]", message);
                break; //TODO to think, may be sometimes we need to validate all
            }
        }
    }

    public void fillObject(StoredObject receiver, DynamicForm form) {
        InputTemplate.BindResult bindResult = inputTemplate.getObject(form, name);
        Object value = bindResult.getValue();
        receiver.put(name, value);
    }

    public Object getValue(DynamicForm form) {
        InputTemplate.BindResult bindResult = inputTemplate.getObject(form, name);
        return bindResult.getValue();
    }

    public InputTemplateConfig getInputConfiguration() {
        return inputConfiguration;
    }

    public void fillForm(DynamicForm form, StoredObject object) {
        inputTemplate.fillForm(form, name, object.get(name));
    }
}
