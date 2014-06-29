package models.forms;

import models.forms.inputtemplate.*;
import models.newserialization.SerializableTreeSerializationType;

/**
 * Created by ilya
 */
public class InputTemplateSerializationType extends SerializableTreeSerializationType<InputTemplate> {

    public InputTemplateSerializationType() {
        registerClass("boolean", BooleanInputTemplate.class);
        registerClass("string", StringInputTemplate.class);
        registerClass("date", DateInputTemplate.class);
        registerClass("datetime", DateTimeInputTemplate.class);
        registerClass("address", AddressInputTemplate.class);
        registerClass("password", PasswordInputTemplate.class);
        registerClass("multiline", MultilineInputTemplate.class);
        registerClass("int", IntegerInputTemplate.class);
        registerClass("json", JsonInputTemplate.class);
        registerClass("json list", JsonListInputTemplate.class);
        registerClass("dropdown", DropdownInputTemplate.class);
    }
}
