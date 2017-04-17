package models.forms;

import models.forms.inputtemplate.InputTemplate;
import models.forms.validators.Validator;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import play.twirl.api.Html;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 22:49
 */
public class InputField implements SerializableUpdatable {

    public static final String FIELDS_SEPARATOR_REGEX = "\\|";

    private String name;
    private InputTemplate inputTemplate;
    private boolean skipForEdit; //do not create form entry for it (edit form)
    private boolean required;
    private boolean store;
    private boolean extra; //do not create form entry for it
    private List<Validator> validators;

    public InputField() {
        //emtpy constructor
    }

    public InputField(String name, InputTemplate inputTemplate, boolean required, List<Validator> validators) {
        this.name = name;
        this.inputTemplate = inputTemplate;
        this.required = required;
        this.validators = validators;

        this.store = true;
        this.skipForEdit = false;
        this.extra = false;
    }

    public String getName() {
        return name;
    }

    public String[] getNamePrefixes() {
        String[] elements = name.split(FIELDS_SEPARATOR_REGEX);
        String[] prefixes = new String[elements.length - 1];
        System.arraycopy(elements, 0, prefixes, 0, elements.length - 1);
        return prefixes;
    }

    public String getLastName() {
        String[] elements = name.split(FIELDS_SEPARATOR_REGEX);
        return elements[elements.length - 1];
    }

    public InputTemplate getInputTemplate() {
        return inputTemplate;
    }

    public Html format(RawForm form) {
        return inputTemplate.render(form, name);
    }

    public List<? extends Validator> getValidators() {
        return validators;
    }

    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        inputTemplate = SerializationTypesRegistry.INPUT_TEMPLATE.read(deserializer, "view");

        if (inputTemplate == null) {
            skipForEdit = true;
            store = true;
            required = false;
            validators = new ArrayList<>();
            extra = true;
            return;
        }

        skipForEdit = deserializer.readBoolean("skip for edit", false);
        store = deserializer.readBoolean("store", true);
        required = deserializer.readBoolean("required", false);
        validators = SerializationTypesRegistry.list(SerializationTypesRegistry.VALIDATOR).read(deserializer, "validators");
        extra = false;
    }

    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        SerializationTypesRegistry.INPUT_TEMPLATE.write(serializer, "view", inputTemplate);
        serializer.write("skip for edit", skipForEdit);
        serializer.write("store", store);
        serializer.write("required", required);
        SerializationTypesRegistry.list(SerializationTypesRegistry.VALIDATOR).write(serializer, "validators", validators);
    }

    public boolean isExtra() {
        return extra;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isSkipForEdit() {
        return skipForEdit;
    }

    public boolean isStore() {
        return store;
    }

    public String getTitle() {
        return inputTemplate.getTitle();
    }
}
