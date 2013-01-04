package models.fields.inputtemplate;

import play.api.templates.Html;
import play.data.DynamicForm;
import views.html.fields.multiline;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:25
 */
public class MultilineInputTemplate extends InputTemplate {

    @Override
    public Html format(DynamicForm form, String field, InputTemplateConfig config) {
        return multiline.render(form, field, config.isRequired(), config.getTitle(), config.getPlaceholder());
    }

    @Override
    public BindResult getObject(DynamicForm form, String field) {
        String value = form.field(field).value();
        if (value.equals(""))
            value = null;
        return new BindResult(value);
    }

    @Override
    public DynamicForm fillForm(DynamicForm form, String field, Object value) {
        return setFormField(form, field, value);
    }
}
