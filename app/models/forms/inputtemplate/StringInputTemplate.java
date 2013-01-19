package models.forms.inputtemplate;

import play.api.templates.Html;
import play.data.DynamicForm;
import views.html.fields.text;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:24
 */
public class StringInputTemplate extends InputTemplate {

    @Override
    public Html format(DynamicForm form, String field, InputTemplateConfig config) {
        return text.render("input", form, field, config.getPlaceholder());
    }

    @Override
    public BindResult getObject(DynamicForm form, String field) {
        String value = form.field(field).value();
        if (value != null && value.equals(""))
            value = null;
        return new BindResult(value);
    }

    @Override
    public void fillForm(DynamicForm form, String field, Object value) {
        if (value == null)
            removeFormField(form, field);
        else
            setFormField(form, field, (String) value);
    }
}
