package models;

import models.forms.InputForm;
import models.serialization.MemoryDeserializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.01.13
 * Time: 19:45
 */
public class Forms {

    public static String LOGIN_FORM_LOGIN = "login";
    public static String LOGIN_FORM_PASSWORD = "password";
    public static String PASSWORD_REMIND_FORM_EMAIL_OR_LOGIN = "email_or_login";

    @SuppressWarnings("unchecked")
    public static InputForm loginForm = InputForm.deserialize("login",
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", LOGIN_FORM_LOGIN,
                                    "type", "string",
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", LOGIN_FORM_PASSWORD,
                                    "type", "password",
                                    "required", true
                            )
                    ),
                    "validators",
                    Utils.listify(
                        Utils.mapify("type", "authenticator")
                    )
            )
    );

    @SuppressWarnings("unchecked")
    public static InputForm passwordRemindForm = InputForm.deserialize("remind",
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", PASSWORD_REMIND_FORM_EMAIL_OR_LOGIN,
                                    "type", "string",
                                    "required", true
                            )
                    )
            )
    );

    public static InputForm getLoginForm() {
        return loginForm;
    }

    public static InputForm getPasswordRemindForm() {
        return passwordRemindForm;
    }
}
