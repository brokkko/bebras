package models.newserialization;

import models.forms.InputField;
import models.forms.InputForm;
import models.forms.RawForm;
import models.forms.inputtemplate.InputTemplate;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by ilya
 */
public class FormSerializer extends Serializer {

    private final RawForm rawForm;
    private final InputForm form;
    private final String prefix;

    public FormSerializer(InputForm form) {
        this.form = form;
        this.rawForm = new RawForm();
        this.prefix = "";
    }

    private FormSerializer(InputForm form, RawForm rawForm, String prefix) {
        this.form = form;
        this.rawForm = rawForm;
        this.prefix = prefix;
    }

    public RawForm getRawForm() {
        return rawForm;
    }

    // implement Serializer

    private void writeObject(String field, Object value) {
        InputField inputField = form.getField(prefix + field);
        if (inputField == null) //don't store values that are not in the specification
            return;
        if (!inputField.isStore())
            return;

        InputTemplate inputTemplate = inputField.getInputTemplate();
        //noinspection unchecked
        inputTemplate.write(inputField.getName(), value, rawForm);
    }

    @Override
    public void write(String field, int value) {
        writeObject(field, value);
    }
    @Override
    public void write(String field, long value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, double value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, boolean value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, String value) {
        writeObject(field, value);
    }

    @Override
    public Serializer getSerializer(String field) {
        InputField inputField = form.getField(prefix + field);
        if (inputField == null)
            return new FormSerializer(form, rawForm, prefix + field + "|");
        if (!inputField.isStore())
            return new EmptySerializer();

        JSONSerializer j = new JSONSerializer();

        InputTemplate inputTemplate = inputField.getInputTemplate();
        //noinspection unchecked
        inputTemplate.write(inputField.getName(), j.getNode(), rawForm); //only jsonInputTemplate works

        return j;
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        InputField inputField = form.getField(prefix + field);
        if (inputField == null || !inputField.isStore())
            return new EmptyListSerializer();

        JSONListSerializer j = new JSONListSerializer();

        InputTemplate inputTemplate = inputField.getInputTemplate();
        //noinspection unchecked
        inputTemplate.write(inputField.getName(), j.getNode(), rawForm); //only jsonListInputTemplate works

        return j;
    }

    // override instead of implement

    @Override
    public void write(String field, byte value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, short value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, float value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, Date value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, ObjectId value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, byte[] value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, char value) {
        writeObject(field, value);
    }

    @Override
    public void writeNull(String field) {
        writeObject(field, null);
    }

    @Override
    public void write(String field, Serializable value) {
        writeObject(field, value);
    }
}
