package models.newmodel.inputtemplate;

import models.newmodel.InputField;
import models.newmodel.RawForm;
import play.api.templates.Html;
import views.html.fields.text;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:25
 */
public class PasswordInputTemplate extends InputTemplate {

    @Override
    public Html format(RawForm form, InputField inputField) {
        return text.render("password", form, inputField.getName(), inputField.getPlaceholder());
    }

    @Override
    public void write(String field, Object value, RawForm rawForm) {
        rawForm.remove(field);
    }

    @Override
    public Object read(String field, RawForm form) {
        if (form.isEmptyValue(field))
            return null;
        return form.get(field);
    }
}
