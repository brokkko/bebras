package models.newmodel.inputtemplate;

import models.newmodel.InputField;
import models.newmodel.RawForm;
import play.api.templates.Html;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 23:42
 */
public abstract class InputTemplate { //Object -> String fields, String fields -> Object

    public static final Map<String, InputTemplate> registeredTemplates = new HashMap<>();

    static {
        registeredTemplates.put("boolean", new BooleanInputTemplate());
        registeredTemplates.put("string", new StringInputTemplate());
        registeredTemplates.put("date", new DateInputTemplate());
        registeredTemplates.put("address", new AddressInputTemplate());
        registeredTemplates.put("password", new PasswordInputTemplate());
        registeredTemplates.put("multiline", new MultilineInputTemplate());
    }

    public static InputTemplate getInstance(String name) {
        return registeredTemplates.get(name);
    }

    public abstract Html format(RawForm form, InputField inputField);

    public abstract void write(String field, Object value, RawForm rawForm);

    public abstract Object read(String field, RawForm form);
}