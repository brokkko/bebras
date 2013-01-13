package models.forms.inputtemplate;

import play.api.templates.Html;
import play.data.DynamicForm;

import java.util.*;

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
            case "date":
                return new DateInputTemplate();
        }
        throw new IllegalArgumentException("Unknown input template type '" + type + "'");
        //TODO error in template for @form(field).errors().map { e => @e.message }
    }

    protected DynamicForm setFormField(DynamicForm form, String field, Object value) {
        //TODO is there an easier way?
        Map<String, String> map = new HashMap<>();
        map.put(field, (String)value);
        return form.bind(map);
    }

    public class BindResult {
        private Object value;
        private List<String> messages;

        protected BindResult(Object value, List<String> messages) {
            this.value = value;
            this.messages = messages;
        }

        protected BindResult(Object value, String... messages) {
            this.value = value;
            if (messages.length == 0)
                this.messages = null;
            else
                this.messages = Arrays.asList(messages);
        }

        public Object getValue() {
            return value;
        }

        public List<String> getMessages() {
            return messages;
        }

        public boolean hasErrors() {
            return messages != null && messages.size() > 0;
        }
    }

    public abstract Html format(DynamicForm form, String field, InputTemplateConfig config);

    public abstract BindResult getObject(DynamicForm form, String field);

    public abstract DynamicForm fillForm(DynamicForm form, String field, Object value);

}
