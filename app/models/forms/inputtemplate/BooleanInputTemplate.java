package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationType;
import models.newserialization.Serializer;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.01.13
 * Time: 13:49
 */
public class BooleanInputTemplate extends InputTemplate<Boolean> {

    private String hint;
    private boolean defaultValue;
    private boolean falseIsNull;

    @Override
    public Html render(RawForm form, String field) {
        return views.html.fields.checkbox.render(read(field, form), field, hint);
    }

    @Override
    public void write(String field, Boolean value, RawForm rawForm) {
        if (value != null && value)
            rawForm.put(field, "1");
        else
            rawForm.remove(field);
    }

    @Override
    public Boolean read(String field, RawForm form) {
        String formValue = form.get(field);
        boolean value = defaultValue;
        if (formValue != null)
            value = formValue.equals("1");

        if (falseIsNull)
            return !value ? null : true;
        else
            return value;
    }

    @Override
    public SerializationType<Boolean> getType() {
        return new BasicSerializationType<>(Boolean.class);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        hint = deserializer.readString("hint", "");
        defaultValue = deserializer.readBoolean("default", false);
        falseIsNull = deserializer.readBoolean("false is null", false);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("hint", hint);
        serializer.write("default", defaultValue);
        if (falseIsNull)
            serializer.write("false is null", true);
    }
}
