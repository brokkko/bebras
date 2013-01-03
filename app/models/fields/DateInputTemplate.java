package models.fields;

import play.api.templates.Html;
import play.data.DynamicForm;
import views.html.fields.date;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:24
 */
public class DateInputTemplate extends InputTemplate {

    @Override
    public Html format(DynamicForm form, String field, Map<String, Object> config) {
        return date.render(form, field, (Boolean) config.get("required"), (String) config.get("title"));
    }

    @Override
    public Object fillObject(DynamicForm form, String field) {
        return null;
    }

    @Override
    public void fillForm(DynamicForm form, String field, Object value) {
    }
}
