package models.fields.inputtemplate;

import models.Event;
import models.fields.InputField;
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
                    return Messages.get("form." + Event.current().getId() + "." + field.getForm().getName() + "." + field.getName() + "." + key);
            }

        return value;
    }

    public boolean isRequired() {
        return (Boolean) get("required");
    }

    public String getTitle() {
        return (String) get("title");
    }

    public String getPlaceholder() {
        return (String) get("placeholder");
    }
}
