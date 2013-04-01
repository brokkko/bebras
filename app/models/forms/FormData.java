package models.forms;

import play.data.DynamicForm;
import play.i18n.Messages;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.03.13
 * Time: 14:23
 */
public class FormData {

    private Map<String, FormField> fields = new HashMap<>();
    private List<String> errors = new ArrayList<>();

    public FormData(InputForm inputForm) {
        this(inputForm, new DynamicForm().bindFromRequest());
    }

    public FormData(InputForm inputForm, DynamicForm dynamicForm) {

    }

    public FormField get(String name) {
        FormField field = fields.get(name);
        if (field == null) {
            field = new FormField(name, null);
            fields.put(name, field);
        }

        return field;
    }

    public void reject(String message) {
        errors.add(Messages.get(message));
    }

    public void reject(String name, String message) {
        get(name).reject(message);
    }

    public boolean hasErrors() {
        if (errors.size() > 0)
            return true;
        for (FormField field : fields.values())
            if (field.hasErrors())
                return true;

        return false;
    }

}
