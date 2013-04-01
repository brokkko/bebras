package models.forms;

import play.i18n.Messages;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.03.13
 * Time: 14:34
 */
public class FormField {

    private String name;
    private String value;
    private List<String> errors;

    public FormField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public FormField(String name, String value, List<String> errors) {
        this.name = name;
        this.value = value;
        this.errors = errors;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public List<? extends String> getErrors() {
        return errors;
    }

    public void reject(String message) {
        errors.add(Messages.get(message));
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }
}
