package models.forms.validators;

import models.newserialization.SerializableTreeSerializationType;

/**
 * Created by ilya
 */
public class ValidatorSerializationType extends SerializableTreeSerializationType<Validator> {

    public ValidatorSerializationType() {
        registerClass("authenticator", AuthenticatorValidator.class);
        registerClass("code word", CodeWordValidator.class);
        registerClass("date", DateValidator.class);
        registerClass("email", EmailValidator.class);
        registerClass("file list", FileListValidator.class);
        registerClass("pattern", PatternValidator.class);
        registerClass("phone", PhoneValidator.class);
        registerClass("user field", UserFieldValidator.class);
        registerClass("int", IntegerValidator.class);
        registerClass("boolean", BooleanValidator.class);
        registerClass("event has contest", CurrentEventHasContestValidator.class);
        registerClass("event id", EventIdValidator.class);
    }

}
