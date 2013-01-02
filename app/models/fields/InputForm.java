package models.fields;

import models.StoredObject;
import play.api.templates.Html;
import play.data.DynamicForm;
import views.html.helper.form;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 22:04
 */
public class InputForm {

    private List<InputField> fields;
    private List<InputValidator> validators;

    public InputForm(StoredObject storedObject) {
        ArrayList<InputField> fields = new ArrayList<>();

        for (Object field : storedObject.getList("fields"))
            fields.add(new InputField((StoredObject) field));

        this.fields = fields;

        ArrayList<InputValidator> validators = new ArrayList<>();

        List validatorsConfig = storedObject.getList("validators");
        if (validatorsConfig != null)
            for (Object validator : validatorsConfig) {
                StoredObject validatorConfig = (StoredObject) validator;
                validators.add(
                        InputValidator.getInstance(validatorConfig.getString("type"), validatorConfig.toMap())
                );
            }
        this.validators = validators;
    }

    public void validate(DynamicForm form) {
        for (InputField field : fields)
            field.validate(form);

        for (InputValidator validator : validators)
            validator.validate(form);
    }

    public Html format(DynamicForm form) {
        return views.html.fields.form.render(form, fields, controllers.routes.Application.registration());
    }
}
