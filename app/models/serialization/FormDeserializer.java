package models.serialization;

import models.forms.InputField;
import models.forms.InputForm;
import models.forms.RawForm;
import models.forms.inputtemplate.InputTemplate;
import models.forms.validators.Validator;
import play.i18n.Messages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 05.04.13
 * Time: 19:15
 */
public class FormDeserializer implements Deserializer, ListDeserializer {

    private final InputForm inputForm;
    private final RawForm rawForm;
    private final Map<String, Object> values;
    private final Map<String, Object> validationData = new HashMap<>();
    private int listIndex = 0;

    public FormDeserializer(InputForm inputForm) {
        this.inputForm = inputForm;
        this.rawForm = new RawForm();
        this.values = new HashMap<>();

        this.rawForm.bindFromRequest();
        deserializeForm();
    }

    public FormDeserializer(InputForm inputForm, RawForm rawForm) {
        this.inputForm = inputForm;
        this.rawForm = rawForm;
        this.values = new HashMap<>();

        deserializeForm();
    }

    private FormDeserializer(Map<String, Object> values) {
        this.inputForm = null;
        this.rawForm = null;
        this.values = values;
    }

    private void deserializeForm() {
        Collection<? extends InputField> fields = inputForm.getFields();
        for (InputField inputField : fields)
            deserializeField(inputField);

        //noinspection unchecked
        for (Validator<FormDeserializer> validator : inputForm.getValidators()) {
            String error = validator.validate(this);
            if (error != null)
                rawForm.reject(error);
        }
    }

    private void deserializeField(InputField inputField) {
        String[] prefixes = inputField.getNamePrefixes();
        String fieldName = inputField.getLastName();

        Map<String, Object> values = this.values;

        /* TODO report. replace with for each intension removes unchecked and supposes the wrong name
        for (int i = 0; i < prefixes.length; i++)
            //noinspection unchecked
            values = (Map<String, Object>) values.get(prefixes[i]);
        */

        for (String prefix : prefixes)
            //noinspection unchecked
            values = (Map<String, Object>) values.get(prefix);

        InputTemplate inputTemplate = inputField.getInputTemplate();
        Object value = inputTemplate.read(fieldName, rawForm);

        //validate field
        if (value != null)
            for (Validator<Object> validator : inputField.getValidators()) {
                String error = validator.validate(value);
                if (error != null)
                    rawForm.reject(fieldName, error);
            }
        else if (inputField.isRequired())
            rawForm.reject(fieldName, Messages.get("error.msg.required"));

        if (rawForm.hasFieldErrors(fieldName))
            value = null;

        if (value != null)
            values.put(fieldName, value);
        else
            values.remove(fieldName);
    }

    public RawForm getRawForm() {
        return rawForm;
    }

    public void putValidationData(String field, Object value) {
        validationData.put(field, value);
    }

    public Object getValidationData(String field) {
        return validationData.get(field);
    }

    //  implement deserializer

    @Override
    public int getInt(String field) {
        return (Integer) getObject(field);
    }

    @Override
    public Boolean getBoolean(String field) {
        return (Boolean) getObject(field);
    }

    @Override
    public String getString(String field) {
        return (String) getObject(field);
    }

    @Override
    public Object getObject(String field) {
        return values.get(field);
    }

    @Override
    public FormDeserializer getDeserializer(String field) {
        //noinspection unchecked
        Map<String, Object> map = (Map<String, Object>) values.get(field);
        return map == null ? null : new FormDeserializer(map);
    }

    @Override
    public FormDeserializer getListDeserializer(String field) {
        return getDeserializer(field);
    }

    @Override
    public Set<String> fieldSet() {
        return values.keySet();
    }

//  implement list deserializer

    @Override
    public boolean hasMore() {
        return values.containsKey(String.valueOf(listIndex));
    }

    @Override
    public int getInt() {
        return (Integer) getObject();
    }

    @Override
    public boolean getBoolean() {
        return (Boolean) getObject();
    }

    @Override
    public String getString() {
        return (String) getObject();
    }

    @Override
    public Object getObject() {
        return getObject(String.valueOf(listIndex++));
    }

    @Override
    public FormDeserializer getDeserializer() {
        return getDeserializer(String.valueOf(listIndex++));
    }

    @Override
    public FormDeserializer getListDeserializer() {
        return getDeserializer();
    }
}
