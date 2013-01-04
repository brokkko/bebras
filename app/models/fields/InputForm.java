package models.fields;

import models.MongoObject;
import models.StoredObject;
import models.fields.validators.Validator;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 22:04
 */
public class InputForm {

    private String name;
    private List<InputField> fields;
    private List<Validator> validators;

    public InputForm(String name, StoredObject storedObject) {
        this.name = name;

        ArrayList<InputField> fields = new ArrayList<>();

        for (Object field : storedObject.getList("fields"))
            fields.add(new InputField(this, (StoredObject) field));

        this.fields = fields;

        ArrayList<Validator> validators = new ArrayList<>();

        List validatorsConfig = storedObject.getList("validators");
        if (validatorsConfig != null)
            for (Object validator : validatorsConfig) {
                StoredObject validatorConfig = (StoredObject) validator;
                validators.add(
                        Validator.getInstance(validatorConfig.getString("type"), validatorConfig.toMap())
                );
            }
        this.validators = validators;
    }

    public Html format(DynamicForm form, String submitMessage) {
        return views.html.fields.form.render(form, Messages.get(submitMessage), fields);
    }

    public String getName() {
        return name;
    }

    public void getObject(StoredObject receiver, DynamicForm form) {
        for (InputField field : fields)
            field.validate(receiver, form);

        for (Validator validator : validators) {
            String message = validator.validate(form); //TODO think about global forms validators
            if (message != null)
                form.reject(message);
        }
    }

}
