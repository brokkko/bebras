package models.fields;

import play.api.templates.Html;
import play.data.DynamicForm;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 21:31
 */
public abstract class InputTemplate {

    public static InputTemplate getInstance(String type) {
        switch (type) {
            case "string":
                return new StringInputTemplate();
            case "password":
                return new PasswordInputTemplate();
            case "multiline":
                return new MultilineInputTemplate();
        }
        throw new IllegalArgumentException("Unknown input template type '" + type + "'");
    }

    public abstract Html format(DynamicForm form, String field, Map<String, Object> config);

}
