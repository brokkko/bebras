package models.newmodel.inputtemplate;

import models.newmodel.InputField;
import models.newmodel.RawForm;
import play.api.templates.Html;
import views.html.fields.multiline;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:25
 */
public class MultilineInputTemplate extends InputTemplate {

    @Override
    public Html format(RawForm form, InputField inputField) {
        return multiline.render(form, inputField.getName(), inputField.getPlaceholder());
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
