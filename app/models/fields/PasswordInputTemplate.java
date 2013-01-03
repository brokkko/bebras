package models.fields;

import play.api.templates.Html;
import play.data.DynamicForm;
import views.html.fields.text;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:25
 */
public class PasswordInputTemplate extends InputTemplate {
    @Override
    public Html format(DynamicForm form, String field, Map<String, Object> config) {
        return text.render("password", form, field, (Boolean) config.get("required"), (String) config.get("title"), (String) config.get("placeholder"));
    }

    @Override
    public Object fillObject(DynamicForm form, String field) {
        return null;  //TODO implement
    }

    @Override
    public void fillForm(DynamicForm form, String field, Object value) {
        //TODO implement
    }
}
