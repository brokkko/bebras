package models.newmodel;

import models.Event;
import models.newmodel.inputtemplate.InputTemplate;
import models.newmodel.validators.Validator;
import play.api.templates.Html;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 22:49
 */
public class InputField {

    public static final String FIELDS_SEPARATOR = "|";

    private String name;
    private String messagesPrefix;
    private InputTemplate inputTemplate;
    private Map<String, Object> additionalConfiguration;
    private List<Validator> validators;

    public InputField(String name, String messagesPrefix, InputTemplate inputTemplate, Map<String, Object> additionalConfiguration, List<Validator> validators) {
        this.name = name;
        this.inputTemplate = inputTemplate;
        this.additionalConfiguration = additionalConfiguration;
        this.validators = validators;
        this.messagesPrefix = messagesPrefix;
    }

    public String getName() {
        return name;
    }

    public String[] getNamePrefixes() {
        String[] elements = name.split(FIELDS_SEPARATOR);
        String[] prefixes = new String[elements.length - 1];
        System.arraycopy(elements, 0, prefixes, 0, elements.length - 1);
        return prefixes;
    }

    public String getLastName() {
        String[] elements = name.split(FIELDS_SEPARATOR);
        return elements[elements.length - 1];
    }

    public InputTemplate getInputTemplate() {
        return inputTemplate;
    }

    public Html format(RawForm form) {
        return inputTemplate.format(form, this);
    }

    public static InputField deserialize(String messagesPrefix, Deserializer deserializer) {
        String name = null;
        InputTemplate inputTemplate = null;
        Map<String, Object> additionalConfiguration = new HashMap<>();
        List<Validator> validators = new ArrayList<>();

        for (String fieldName : deserializer.fieldSet()) {
            switch (fieldName) {
                case "name":
                    name = deserializer.getString(fieldName);
                    break;
                case "type":
                    inputTemplate = InputTemplate.getInstance(deserializer.getString(fieldName));
                    break;
                case "validators":
                    ListDeserializer validatorsDeserializer = deserializer.getListDeserializer(fieldName);
                    while (validatorsDeserializer.hasMore()) {
                        Deserializer validatorDeserializer = validatorsDeserializer.getDeserializer();
                        validators.add(Validator.deserialize(validatorDeserializer));
                    }
                    break;
                default:
                    additionalConfiguration.put(fieldName, deserializer.getObject(fieldName));
            }
        }

        return new InputField(name, messagesPrefix, inputTemplate, additionalConfiguration, validators);
    }

    //standard configuration

    public boolean isRequired() {
        Boolean required = (Boolean) additionalConfiguration.get("required");
        return required == null ? false : required;
    }

    public Object getConfig(String name) {
        return additionalConfiguration.get(name);
    }

    public String getConfigFromMessages(String key) {
        String title = (String) additionalConfiguration.get(key);
        if (title == null)
            return Messages.get("form." + Event.current().getId() + "." + messagesPrefix + "." + name + "." + key); //TODO get messages name ??
        return title;
    }

    public String getTitle() {
        return getConfigFromMessages("title");
    }

    public String getPlaceholder() {
        return getConfigFromMessages("placeholder");
    }
}
