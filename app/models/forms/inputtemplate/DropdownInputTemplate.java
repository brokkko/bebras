package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newserialization.*;
import play.api.templates.Html;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.01.13
 * Time: 13:49
 */
public class DropdownInputTemplate extends InputTemplate<String> {

    private String placeholder;
    private List<String> variants;
    private List<String> titles;

    @Override
    public Html render(RawForm form, String field) {
        return views.html.fields.dropdown.render(form, field, placeholder, variants, titles);
    }

    @Override
    public void write(String field, String value, RawForm rawForm) {
        if (value != null)
            rawForm.put(field, value);
        else
            rawForm.remove(field);
    }

    @Override
    public String read(String field, RawForm form) {
        String value = form.get(field);
        if (value != null && value.isEmpty())
            return null;
        return value;
    }

    @Override
    public SerializationType<String> getType() {
        return new BasicSerializationType<>(String.class);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        placeholder = deserializer.readString("placeholder", "");
        variants = SerializationTypesRegistry.list(String.class).read(deserializer, "variants");
        titles = SerializationTypesRegistry.list(String.class).read(deserializer, "titles");

        if (titles == null || titles.size() == 0)
            titles = variants;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        if (placeholder != null && !placeholder.isEmpty())
            serializer.write("placeholder", placeholder);
        SerializationTypesRegistry.list(String.class).write(serializer, "variants", variants);
        if (titles != variants)
            SerializationTypesRegistry.list(String.class).write(serializer, "titles", titles);
    }
}
