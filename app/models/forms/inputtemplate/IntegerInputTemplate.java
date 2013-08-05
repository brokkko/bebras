package models.forms.inputtemplate;

import models.Utils;
import models.forms.RawForm;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationType;
import models.newserialization.Serializer;
import play.api.templates.Html;
import play.i18n.Messages;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.07.13
 * Time: 17:17
 */
public class IntegerInputTemplate extends InputTemplate<Integer> {

    private String placeholder;

    @Override
    public Html render(RawForm form, String field) {
        return views.html.fields.text.render("input", form, field, placeholder);
    }

    @Override
    public void write(String field, Integer value, RawForm rawForm) {
        if (value != null)
            rawForm.put(field, "" + value);
    }

    @Override
    public Integer read(String field, RawForm form) {
        String value = form.get(field);
        if (value == null)
            return null;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            form.reject(field, Messages.get("error.msg.int"));
            return null;
        }
    }

    @Override
    public SerializationType<Integer> getType() {
        return new BasicSerializationType<>(Integer.class);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        placeholder = deserializer.readString("placeholder", "");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("placeholder", placeholder);
    }
}
