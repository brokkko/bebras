package models.forms;

import play.data.DynamicForm;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 05.04.13
 * Time: 16:43
 */
public class RawForm {

    private final Map<String, Object> map;
    private List<String> globalErrors = null;
    private final Map<String, List<String>> errors = new HashMap<>();

    public RawForm(Map<String, Object> map) {
        this.map = map;
    }

    public RawForm() {
        map = new HashMap<>();
    }

    public void bindFromRequest() {
        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        map.clear();
        map.putAll(form.data());
    }

    public void reject(String error) {
        if (globalErrors == null)
            globalErrors = new ArrayList<>();
        globalErrors.add(error);
    }

    public void reject(String field, String error, String... postfixes) {
        List<String> fieldErrors = errors.get(postfixField(field, postfixes));

        if (fieldErrors == null) {
            fieldErrors = new ArrayList<>();
            errors.put(field, fieldErrors);
        }

        fieldErrors.add(error);
    }

    public String get(String field, String... postfixes) {
        return stringify(map.get(postfixField(field, postfixes)));
    }

    public int getAsInt(String field, int defaultValue, String... postfixes) {
        String value = get(field, postfixes);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean isEmptyValue(String field, String... postfixes) {
        String value = get(field, postfixes);
        return value == null || value.equals("");
    }

    public void put(String field, Object value, String... postfixes) {
        map.put(postfixField(field, postfixes), value);
    }

    public void remove(String field, String... postfixes) {
        map.remove(postfixField(field, postfixes));
    }

    public boolean hasErrors() {
        return globalErrors != null || errors.keySet().size() > 0;
    }

    public boolean hasGlobalErrors() {
        return globalErrors != null;
    }

    public boolean hasFieldErrors(String field) {
        return errors.containsKey(field);
    }

    public List<? extends String> getGlobalErrors() {
        return globalErrors;
    }

    public List<? extends String> getFieldErrors(String field) {
        return errors.get(field);
    }

    private String stringify(Object value) {
        return value == null ? null : value.toString();
    }

    public Set<String> keys() {
        return map.keySet();
    }

    public static String postfixField(String field, String... postfixes) {
        StringBuilder res = new StringBuilder(field);

        for (String prefix : postfixes)
            res.append('[').append(prefix).append(']');

        return res.toString();
    }

    @Override
    public String toString() {
        return "RawForm[" + map.toString() + "]";
    }
}
