package models.serialization;

import models.forms.InputField;
import models.forms.InputForm;
import models.forms.RawForm;
import models.forms.inputtemplate.InputTemplate;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 12:48
 */
public class FormSerializer implements Serializer, ListSerializer {

    private final RawForm rawForm;
    private final InputForm form;
    private final String[] prefixes;
    private int nextListIndex = 0;

    public FormSerializer(InputForm form, String... prefixes) {
        this.form = form;
        this.prefixes = prefixes;
        this.rawForm = new RawForm();
    }

    private FormSerializer(InputForm form, RawForm rawForm, String... prefixes) {
        this.form = form;
        this.prefixes = prefixes;
        this.rawForm = rawForm;
    }

    public RawForm getRawForm() {
        return rawForm;
    }

    //implement Serializer

    @Override
    public void write(String field, Object value) {
        field = prefixField(field);

        InputField inputField = form.getField(field);
        if (inputField == null) //don't serialize values that are not in the specification
            return;
        if (!inputField.getBooleanConfig("store", true)) //TODO this is not DRY, "store", true is already written in Event
            return;

        InputTemplate inputTemplate = inputField.getInputTemplate();
        inputTemplate.write(inputField.getName(), value, rawForm);
    }

    @Override
    public FormSerializer getSerializer(String field) {
        int l = prefixes.length;
        String[] newPrefixes = new String[l + 1];
        System.arraycopy(prefixes, 0, newPrefixes, 0, l);
        newPrefixes[l] = field;

        return new FormSerializer(form, rawForm, newPrefixes);
    }

    @Override
    public FormSerializer getListSerializer(String field) {
        return getSerializer(field);
    }

    //implement ListSerializer

    @Override
    public void write(Object value) {
        String field = String.valueOf(nextListIndex++);
        write(field, value);
    }

    @Override
    public FormSerializer getSerializer() {
        int l = prefixes.length;
        String[] newPrefixes = new String[l + 1];
        System.arraycopy(prefixes, 0, newPrefixes, 0, l);
        newPrefixes[l] = String.valueOf(nextListIndex++);

        return new FormSerializer(form, rawForm, newPrefixes);
    }

    @Override
    public FormSerializer getListSerializer() {
        return getSerializer();
    }

    // private methods

    private String prefixField(String field) {
        StringBuilder res = new StringBuilder();

        for (String prefix : prefixes)
            res.append(prefix).append(InputField.FIELDS_SEPARATOR_REGEX);

        res.append(field);

        return res.toString();
    }
}
