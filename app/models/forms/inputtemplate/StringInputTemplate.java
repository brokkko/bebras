package models.forms.inputtemplate;

import models.forms.InputField;
import models.forms.RawForm;
import play.api.templates.Html;
import views.html.fields.text;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:24
 */
public class StringInputTemplate extends InputTemplate {

    @Override
    public Html format(RawForm form, InputField inputField) {
        return text.render("input", form, inputField.getName(), inputField.getPlaceholder());
    }

    @Override
    public void write(String field, Object value, RawForm rawForm) {
        if (value == null)
            rawForm.remove(field);
        else
            rawForm.put(field, value);
    }

    @Override
    public Object read(String field, RawForm form) {
        if (form.isEmptyValue(field))
            return null;
        return form.get(field);
    }
}
