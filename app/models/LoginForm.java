package models;

import models.forms.InputForm;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.01.13
 * Time: 19:45
 */
public class LoginForm {

    public static InputForm form = new InputForm("login",
            new MemoryStoredObject(
                    "fields",
                    MemoryStoredObject.listify(
                            MemoryStoredObject.mapify(
                                    "name", "login",
                                    "input",
                                    MemoryStoredObject.mapify(
                                            "type", "string",
                                            "required", true
                                    )
                            ),
                            MemoryStoredObject.mapify(
                                    "name", "password",
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

    public static InputForm getInstance() {
        return form;
    }
}
