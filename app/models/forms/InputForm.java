package models.forms;

import models.Event;
import models.MemoryStoredObject;
import models.StoredObject;
import models.forms.validators.Validator;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.i18n.Messages;
import play.mvc.Call;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 22:04
 */
public class InputForm {

    private String name;
    private LinkedHashMap<String, InputField> fields;
    private List<Validator> validators;

    public InputForm(String name, String jsonDescription) {
        this(name, new MemoryStoredObject(jsonDescription));
    }

    public InputForm(String name, StoredObject storedObject) {
        this.name = name;

        LinkedHashMap<String, InputField> fields = new LinkedHashMap<>();

        for (Object field : storedObject.getList("fields")) {
            InputField inputField = new InputField(this, (StoredObject) field);
            fields.put(inputField.getName(), inputField);
        }

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
        else {
            StoredObject validatorConfig = storedObject.getObject("validator");
            if (validatorConfig != null)
                validators.add(
                        Validator.getInstance(validatorConfig.getString("type"), validatorConfig.toMap())
                );
        }
        this.validators = validators;
    }

    public Html format(DynamicForm form, Call call) {
        String msgKey = "form." + Event.current().getId() + "." + getName() + ".submit";
        return views.html.fields.form.render(form, call, Messages.get(msgKey), new ArrayList<>(fields.values()));
    }

    public String getName() {
        return name;
    }

    public void validate(DynamicForm form) {
        for (InputField field : fields.values())
            field.validate(form);

        if (form.hasErrors())
            return; //don't validate if there are errors already

        FilledInputForm filledForm = new FilledInputForm(form);
        for (Validator validator : validators) {
            String message = validator.validate(filledForm);
            if (message != null)
                form.reject(message);
        }
    }

    public void fillObject(StoredObject receiver, DynamicForm form) {
        for (InputField field : fields.values())
            field.fillObject(receiver, form);
    }

    public StoredObject getObject(DynamicForm form) {
        MemoryStoredObject result = new MemoryStoredObject();
        fillObject(result, form);
        return result;
    }

    public InputField getField(String name) {
        return fields.get(name);
    }

    public class FilledInputForm {
        private DynamicForm form;

        public FilledInputForm(DynamicForm form) {
            this.form = form;
        }

        public DynamicForm getForm() {
            return form;
        }

        public InputForm getInputForm() {
            return InputForm.this;
        }

        public Object get(String field) {
            return getField(field).getValue(form);
        }
    }

}
