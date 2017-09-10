package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationType;
import models.newserialization.Serializer;
import play.twirl.api.Html;
import views.html.fields.multiline;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:25
 */
public class MultilineInputTemplate extends InputTemplate<String> {

    private String placeholder;
    private boolean small;

    @Override
    public Html render(RawForm form, String field) {
        return multiline.render(form, field, placeholder, true, small, hint);
    }

    @Override
    public void write(String field, String value, RawForm rawForm) {
        if (value == null)
            rawForm.remove(field);
        else
            rawForm.put(field, value);
    }

    @Override
    public String read(String field, RawForm form) {
        if (form.isEmptyValue(field))
            return null;
        return form.get(field);
    }

    @Override
    public SerializationType<String> getType() {
        return new BasicSerializationType<>(String.class);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        placeholder = deserializer.readString("placeholder", "");
        small = deserializer.readBoolean("small", false);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("placeholder", placeholder);
        if (small)
            serializer.write("small", small);
    }
}
