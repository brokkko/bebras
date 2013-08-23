package models;

import models.forms.InputForm;
import models.newserialization.MemoryDeserializer;

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
    public static InputForm loginForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", LOGIN_FORM_LOGIN,
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Логин", //TODO do i18n
                                            "placeholder", "Введите логин"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", LOGIN_FORM_PASSWORD,
                                    "view", Utils.mapify(
                                            "type", "password",
                                            "title", "Пароль",
                                            "placeholder", "Введите пароль"
                                    ),
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
    public static InputForm passwordRemindForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", PASSWORD_REMIND_FORM_EMAIL_OR_LOGIN,
                                    "view", Utils.mapify(
                                    "type", "string",
                                    "title", "Email или логин",
                                    "placeholder", "Введите email или логин"
                            ),
                                    "required", true
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    @SuppressWarnings("unchecked")
    private static InputForm contestChangeForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "name",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Название",
                                            "placeholder", "Введите название конкурса"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "start",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата начала"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "finish",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата окончания"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "results",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата отображения результатов"
                                    ),
                                    "required", false
                            ),
                            Utils.mapify(
                                    "name", "duration",
                                    "view", Utils.mapify(
                                            "type", "int",
                                            "placeholder", "Введите количество минут",
                                            "title", "Минут на прохождение"
                                    ),
                                    "validators", Utils.listify(
                                    Utils.mapify(
                                            "type", "int",
                                            "compare", ">=0"
                                    )
                            ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "description",
                                    "view", Utils.mapify(
                                            "type", "multiline",
                                            "placeholder", "Введите описание",
                                            "title", "Описание"
                                    ),
                                    "required", false
                            ),
                            Utils.mapify(
                                    "name", "page sizes",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Размеры страниу для задач",
                                            "title", "Размеры страниц"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "tables",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Введите информацию про таблицы",
                                            "title", "Таблицы с данными"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "allow restart",
                                    "view", Utils.mapify(
                                            "type", "boolean",
                                            "title", "Можно перезапускать",
                                            "hint", "У этого соревнования можно сбросить результаты"
                                    ),
                                    "required", false
                            ),
                            Utils.mapify(
                                    "name", "only admin",
                                    "view", Utils.mapify(
                                            "type", "boolean",
                                            "title", "Только для администраторов",
                                            "hint", "Это соревнование видно только администраторам"
                                    ),
                                    "required", false
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    @SuppressWarnings("unchecked")
    private static InputForm eventChangeForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "title",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Название",
                                            "placeholder", "Введите название конкурса"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "results",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата отображения результатов"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "registration start",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата начала регистрации"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "registration finish",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата завершения регистрации"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "tables",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Введите информацию про таблицы",
                                            "title", "Таблицы с данными"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "roles",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Введите информацию роли пользователей",
                                            "title", "Роли пользователей"
                                    ),
                                    "required", true
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    @SuppressWarnings("unchecked")
    private static InputForm addBlockForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "config",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Строка конфигурации",
                                            "placeholder", "Введите строку конфигурации"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                "type", "problem block configuration"
                                            )
                                    )
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    @SuppressWarnings("unchecked")
    private static InputForm addContestForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "id",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Идентификатор соревнования",
                                            "placeholder", "Введите идентификатор соревнования"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                    "type", "event has contest"
                                            ),
                                            Utils.mapify(
                                                    "type", "pattern",
                                                    "pattern", "[\\-a-zA-Z0-9]+",
                                                    "message", "Идентификатор может содержать только символы a-z, A-Z, 0-9 и тире"
                                            )
                                    )
                            ),
                            Utils.mapify(
                                    "name", "name",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Название соревнования",
                                            "placeholder", "Введите название соревнования"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                    )
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    @SuppressWarnings("unchecked")
    private static InputForm setHtmlBlockForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "html",
                                    "view", Utils.mapify(
                                            "type", "multiline",
                                            "title", "Html-текст блока",
                                            "placeholder", "Введите html-текст блока"
                                    ),
                                    "required", false,
                                    "validators", Utils.listify(
                                    )
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    public static InputForm getLoginForm() {
        return loginForm;
    }

    public static InputForm getPasswordRemindForm() {
        return passwordRemindForm;
    }

    public static InputForm getContestChangeForm() {
        return contestChangeForm;
    }

    public static InputForm getEventChangeForm() {
        return eventChangeForm;
    }

    public static InputForm getAddBlockForm() {
        return addBlockForm;
    }

    public static InputForm getAddContestForm() {
        return addContestForm;
    }

    public static InputForm getSetHtmlBlockForm() {
        return setHtmlBlockForm;
    }
}
