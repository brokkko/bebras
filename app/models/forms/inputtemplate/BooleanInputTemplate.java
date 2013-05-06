package models.forms.inputtemplate;

import models.forms.InputField;
import models.forms.RawForm;
import play.api.templates.Html;
import play.i18n.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.01.13
 * Time: 13:49
 */
public class BooleanInputTemplate extends InputTemplate {

    @Override
    public Html format(RawForm form, InputField inputField) {
        String hintContent = (String) inputField.getConfig("hint");
        String hint = Messages.get(hintContent);
        return views.html.fields.checkbox.render(form, inputField.getName(), hint);
    }

    @Override
    public void write(String field, Object value, RawForm rawForm) {
        if (value != null && (Boolean) value)
            rawForm.put(field, "1");
        else
            rawForm.remove(field);
    }

    @Override
    public Object read(String field, RawForm form) {
        String value = form.get(field);
        return "1".equals(value) ? true : null;
    }
}
