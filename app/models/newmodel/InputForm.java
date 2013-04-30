package models.newmodel;

import models.Event;
import models.newmodel.validators.Validator;
import play.api.templates.Html;
import play.i18n.Messages;
import play.mvc.Call;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 14:37
 */
public class InputForm {

    private String name;
    private LinkedHashMap<String, InputField> fields;
    private List<Validator> validators;

    public InputForm(String name, Collection<InputField> fields, List<Validator> validators) {
        this.name = name;
        this.validators = validators;

        this.fields = new LinkedHashMap<>();
        for (InputField field : fields)
            this.fields.put(field.getName(), field);
    }

    public String getName() {
        return name;
    }

    public InputField getField(String fieldName) {
        return fields.get(fieldName);
    }

    public Collection<? extends InputField> getFields() {
        return fields.values();
    }

    /*
    @Override
    public void store(Serializer serializer) {
        Serializer fieldsSerializer = serializer.getSerializer("fields");
        for (Map.Entry<String, InputField> fieldEntry : fields.entrySet()) {
            Serializer fieldSerializer = fieldsSerializer.getSerializer(fieldEntry.getKey());
            fieldEntry.getValue().store(fieldSerializer);
        }

        ListSerializer validatorsSerializer = serializer.getListSerializer("validators");
        for (Validator validator : validators) {
            Serializer validatorSerializer = validatorsSerializer.getSerializer();
            validator.store(validatorSerializer);
        }
    }
    */

    public static InputForm deserialize(String messagesName, Deserializer deserializer) {
        List<InputField> fields = new ArrayList<>();

        ListDeserializer fieldsDeserializer = deserializer.getListDeserializer("fields");

        while (fieldsDeserializer.hasMore()) {
            Deserializer inputFieldDeserializer = fieldsDeserializer.getDeserializer();
            fields.add(InputField.deserialize(messagesName, inputFieldDeserializer));
        }

        List<Validator> validators = new ArrayList<>();

        ListDeserializer validatorsList = deserializer.getListDeserializer("validators");
        while (validatorsList.hasMore()) {
            Validator validator = Validator.deserialize(validatorsList.getDeserializer());
            validators.add(validator);
        }

        return new InputForm(messagesName, fields, validators);
    }

    public Html format(RawForm form, Call call) {
        return formatExtended(form, call, false);
    }

    public Html formatWithUndo(RawForm form, Call call) {
        return formatExtended(form, call, true);
    }

    private Html formatExtended(RawForm form, Call call, boolean needUndo) {
        String msgKey = "form." + Event.current().getId() + "." + getName() + ".submit";

        return views.html.fields.form.render(this, form, call, Messages.get(msgKey), needUndo);
    }

    public List<Validator> getValidators() {
        return validators;
    }
}
