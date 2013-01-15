package models;

import models.forms.InputForm;

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

    public static InputForm loginForm = new InputForm("login",
            new MemoryStoredObject(
                    "fields",
                    MemoryStoredObject.listify(
                            MemoryStoredObject.mapify(
                                    "name", LOGIN_FORM_LOGIN,
                                    "input",
                                    MemoryStoredObject.mapify(
                                            "type", "string",
                                            "required", true
                                    )
                            ),
                            MemoryStoredObject.mapify(
                                    "name", LOGIN_FORM_PASSWORD,
                                    "input",
                                    MemoryStoredObject.mapify(
                                            "type", "password",
                                            "required", true
                                    )
                            )
                    ),
                    "validator",
                    MemoryStoredObject.mapify("type", "authenticator")
            )
    );

    public static InputForm passwordRemindForm = new InputForm("remind",
            new MemoryStoredObject(
                    "fields",
                    MemoryStoredObject.listify(
                            MemoryStoredObject.mapify(
                                    "name", PASSWORD_REMIND_FORM_EMAIL_OR_LOGIN,
                                    "input",
                                    MemoryStoredObject.mapify(
                                            "type", "string",
                                            "required", true
                                    )
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
