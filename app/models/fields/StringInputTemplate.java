package models.fields;

import play.api.templates.Html;
import play.data.DynamicForm;
import views.html.fields.text;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:24
 */
public class StringInputTemplate extends InputTemplate {

    @Override
    public Html format(DynamicForm form, String field, Map<String, Object> config) {
        return text.render("input", form, field, (Boolean) config.get("required"), (String) config.get("placeholder"));
    }
}
