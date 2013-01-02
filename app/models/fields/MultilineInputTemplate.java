package models.fields;

import play.api.templates.Html;
import play.data.DynamicForm;
import views.html.fields.multiline;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:25
 */
public class MultilineInputTemplate extends InputTemplate {

    @Override
    public Html format(DynamicForm form, String field, Map<String, Object> config) {
        return multiline.render(form, field, (Boolean) config.get("required"), (String) config.get("placeholder"));
    }
}
