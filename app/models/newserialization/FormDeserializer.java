package models.newserialization;

import models.forms.InputField;
import models.forms.InputForm;
import models.forms.RawForm;
import models.forms.inputtemplate.InputTemplate;
import models.forms.validators.Validator;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.i18n.Messages;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ilya
 */
public class FormDeserializer extends Deserializer {

    private final InputForm inputForm;
    private final RawForm rawForm;
    private final Map<String, Object> values;
    private final Map<String, Object> validationData = new HashMap<>();

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

    private void deserializeForm() {
        Collection<? extends InputField> fields = inputForm.getFields();
        for (InputField inputField : fields)
            deserializeField(inputField);

        //noinspection unchecked
        for (Validator<FormDeserializer> validator : inputForm.getValidators()) {
            Validator.ValidationResult result = validator.validate(this);
            if (result.getMessage() != null)
                rawForm.reject(result.getMessage());
            if (result.getValidationData() != null)
                validationData.put("", result.getValidationData());
        }
    }

    private void deserializeField(InputField inputField) {
        String[] prefixes = inputField.getNamePrefixes();
        String fieldName = inputField.getLastName();

        Map<String, Object> values = this.values;

        /* TODO report. replace with for each intention removes unchecked and supposes the wrong name
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
                Validator.ValidationResult result = validator.validate(value);
                if (result.getMessage() != null)
                    rawForm.reject(fieldName, result.getMessage());
                if (result.getValidationData() != null) //TODO make possible to store several validation results
                    validationData.put(fieldName, result.getValidationData());
            }
        else if (inputField.isRequired() && !rawForm.hasFieldErrors(fieldName)) //test required only if there are no other errors
            rawForm.reject(fieldName, Messages.get("error.msg.required"));

        if (rawForm.hasFieldErrors(fieldName))
            value = null;

        if (value != null)
            values.put(fieldName, value);
        else
            values.remove(fieldName);
    }

    public Object getValue(String field) {
        return values.get(field);
    }

    public RawForm getRawForm() {
        return rawForm;
    }

    public Object getValidationData() {
        return validationData.get("");
    }

    public Object getValidationData(String field) {
        return validationData.get(field);
    }

    // implement deserializer

    @Override
    public Integer readInt(String field) {
        return (Integer) values.get(field);
    }

    @Override
    public Long readLong(String field) {
        return (Long) values.get(field);
    }

    @Override
    public Double readDouble(String field) {
        return (Double) values.get(field);
    }

    @Override
    public Boolean readBoolean(String field) {
        return (Boolean) values.get(field);
    }

    @Override
    public String readString(String field) {
        return (String) values.get(field);
    }

    @Override
    public Deserializer getDeserializer(String field) {
        //treats json ObjectNodes as deserializer
        Object value = values.get(field);
        return value == null ? null : new JSONDeserializer((ObjectNode) value);
    }

    @Override
    public ListDeserializer getListDeserializer(String field) {
        //treats json ArrayNodes as listDeserializer
        Object value = values.get(field);
        return value == null ? null : new JSONListDeserializer((ArrayNode) value);
    }

    @Override
    public Collection<String> fields() {
        return values.keySet();
    }

    @Override
    public boolean isNull(String field) {
        return values.get(field) == null;
    }

    // override instead of implement

    @Override
    public Byte readByte(String field) {
        return (Byte) values.get(field);
    }

    @Override
    public Short readShort(String field) {
        return (Short) values.get(field);
    }

    @Override
    public Float readFloat(String field) {
        return (Float) values.get(field);
    }

    @Override
    public Character readChar(String field) {
        return (Character) values.get(field);
    }

    @Override
    public Date readDate(String field) {
        return (Date) values.get(field);
    }

    @Override
    public ObjectId readObjectId(String field) {
        return (ObjectId) values.get(field);
    }

    @Override
    public byte[] readByteArray(String field) {
        return (byte[]) values.get(field);
    }

}
