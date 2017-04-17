package models.forms;

import models.forms.validators.Validator;
import models.newserialization.*;
import play.twirl.api.Html;
import play.i18n.Messages;
import play.mvc.Call;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 14:37
 */
public class InputForm implements SerializableUpdatable {

    public static InputForm deserialize(Deserializer deserializer) {
        InputForm result = new InputForm();
        result.update(deserializer);
        return result;
    }

    private LinkedHashMap<String, InputField> fields = new LinkedHashMap<>();
    private List<Validator> validators = new ArrayList<>();

    public InputForm() {
        //empty constructor
    }

    public InputForm(List<InputField> fieldsList, List<Validator> validators) {
        this.validators = validators;
        setupFields(fieldsList);
    }

    public InputField getField(String fieldName) {
        return fields.get(fieldName);
    }

    public Collection<? extends InputField> getFields() {
        return fields.values();
    }

    public void update(Deserializer deserializer) {
        List<InputField> fieldsList = SerializationTypesRegistry.list(new SerializableSerializationType<>(InputField.class)).read(deserializer, "fields");

        validators = SerializationTypesRegistry.list(SerializationTypesRegistry.VALIDATOR).read(deserializer, "validators");

        setupFields(fieldsList);
    }

    public void serialize(Serializer serializer) {
        SerializationTypesRegistry.list(new SerializableSerializationType<>(InputField.class)).write(serializer, "fields", new ArrayList<>(fields.values()));
        SerializationTypesRegistry.list(SerializationTypesRegistry.VALIDATOR).write(serializer, "validators", validators);
    }

    public Html format(RawForm form, Call call) {
        return formatExtended(form, call, false, null);
    }

    public Html formatExtended(RawForm form, Call call, boolean needUndo, String submitText) {
        return views.html.fields.form.render(this, form, call, Messages.get(submitText), needUndo);
    }

    public List<Validator> getValidators() {
        return validators;
    }

    private void setupFields(List<InputField> fieldsList) {
        fields = new LinkedHashMap<>();
        for (InputField field : fieldsList)
            fields.put(field.getName(), field);
    }

    public InputForm filter(FieldFilter filter) {
        List<InputField> filteredFields = new ArrayList<>();
        for (InputField inputField : fields.values())
            if (filter.accept(inputField))
                filteredFields.add(inputField);

        InputForm result = new InputForm();
        result.validators = new ArrayList<>(validators);
        result.setupFields(filteredFields);

        return result;
    }

    public static interface FieldFilter {
        boolean accept(InputField field);
    }

    public static InputForm union(InputForm first, InputForm second) {
        List<InputField> fields = new ArrayList<>();
        List<Validator> validators = new ArrayList<>();

        fields.addAll(first.fields.values());
        fields.addAll(second.fields.values());
        validators.addAll(first.validators);
        validators.addAll(second.validators);
        
        return new InputForm(fields, validators);
    }
}
