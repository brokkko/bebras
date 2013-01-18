package models.forms.inputtemplate;

import models.Event;
import models.forms.InputField;
import play.api.templates.Html;
import play.i18n.Messages;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 13:27
 */
public class InputTemplateConfig {

    private final Map<String, Object> config;
    private InputField field;

    public InputTemplateConfig(Map<String, Object> config, InputField field) {
        this.config = config;
        this.field = field;
    }

    public Object get(String key) {
        Object value = config.get(key);

        if (value == null)
            switch (key) {
                case "required":
                    return false;
                case "title":
                case "placeholder":
                    String msg = Messages.get("form." + Event.current().getId() + "." + field.getForm().getMessagesName() + "." + field.getName() + "." + key);
                    if (key.equals("title"))
                        return new Html(msg);
                    else
                        return msg;
            }

        return value;
    }

    public boolean isRequired() {
        return (Boolean) get("required");
    }

    public Html getTitle() {
        return (Html) get("title");
    }

    public String getPlaceholder() {
        return (String) get("placeholder");
    }
}
