package models.forms.inputtemplate;

import play.api.templates.Html;
import play.data.DynamicForm;
import play.i18n.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.01.13
 * Time: 13:49
 */
public class BooleanInputTemplate extends InputTemplate {
    @Override
    public Html format(DynamicForm form, String field, InputTemplateConfig config) {
        String hintContent = (String) config.get("hint");
        String hint = Messages.get(hintContent);
        return views.html.fields.checkbox.render(form, field, hint);
    }

    @Override
    public BindResult getObject(DynamicForm form, String field) {
        String value = form.field(field).value();
        boolean result = value != null && value.equals("1");
        return new BindResult(result ? true : null);
    }

    @Override
    public void fillForm(DynamicForm form, String field, Object value) {
        if (value != null && (Boolean) value)
            setFormField(form, field, "1");
        else
            removeFormField(form, field);
    }
}
