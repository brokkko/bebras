package models;

import models.forms.InputForm;
import models.newserialization.MemoryDeserializer;
import models.newserialization.SerializationTypesRegistry;
import models.utils.Utils;

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
                                            "placeholder", "Размеры страниц для задач",
                                            "title", "Размеры страниц",
                                            "small", true
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
                                    "name", "results translators",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Опишите процесс вычисления результата",
                                            "title", "Вычисление результата"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "rights",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Перечислите права для участия",
                                            "title", "Права для участия",
                                            "small", true
                                    ),
                                    "required", true //TODO allow json deserializers to be not required
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
                                    "name", "problems on contests page",
                                    "view", Utils.mapify(
                                            "type", "boolean",
                                            "title", "Без перехода",
                                            "hint", "Задачи этого соревнования будут показаны на странице со списком соревнований"
                                    ),
                                    "required", false
                            ),
                            /*Utils.mapify(
                                    "name", "auto start",
                                    "view", Utils.mapify(
                                            "type", "boolean",
                                            "title", "Автозапуск",
                                            "hint", "Это соревнование будет начинаться автоматически при переходе на страницу соревнований"
                                    ),
                                    "required", false
                            ),*/
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
                                    "name", "restricted results",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата отображения только сокращенных результатов"
                                    ),
                                    "required", false
                            ),
                            Utils.mapify(
                                    "name", "user info closed",
                                    "view", Utils.mapify(
                                            "type", "datetime",
                                            "title", "Дата запрета на редактирование личных данных"
                                    ),
                                    "required", false
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
                                    "name", "skin",
                                    "view", Utils.mapify(
                                            "type", "dropdown",
                                            "title", "Оформление",
                                            "placeholder", "Выберите тип оформления",
                                            "variants", Utils.listify("bbtc", "bebras", "bebras-by", "kio", "issep2018")
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "sso",
                                    "view", Utils.mapify(
                                            "type", "boolean",
                                            "title", "Single Sign-On",
                                            "hint", "Режим единого входа"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "domain",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Домен",
                                            "placeholder", "Введите домен"
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
                                            "placeholder", "Введите информацию о ролях пользователей",
                                            "title", "Роли пользователей"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "results translators",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Опишите процесс вычисления результата",
                                            "title", "Вычисление результата"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "plugins",
                                    "view", Utils.mapify(
                                            "type", "json list",
                                            "placeholder", "Введите информацию о подключенных плагинах",
                                            "title", "Подключенные плагины"
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
                                    "name", "_config",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Строка конфигурации",
                                            "placeholder", "Введите строку конфигурации"
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

    @SuppressWarnings("unchecked")
    private static InputForm cloneEventForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "new_event_id",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Идентификатор нового события",
                                            "placeholder", "Введите идентификатор нового события"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                    "type", "event id",
                                                    "message", "На сервере уже зарегистрировано событие с этим идентификатором"
                                            ),
                                            Utils.mapify(
                                                    "type", "pattern",
                                                    "pattern", "[\\-a-zA-Z0-9]+",
                                                    "message", "Идентификатор может содержать только символы a-z, A-Z, 0-9 и тире"
                                            )
                                    )
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    @SuppressWarnings("unchecked") //now this is the same as cloneEventForm
    private static InputForm copyEventForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "new_event_id",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Идентификатор нового события",
                                            "placeholder", "Введите идентификатор нового события"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                    "type", "event id",
                                                    "message", "На сервере уже зарегистрировано событие с этим идентификатором"
                                            ),
                                            Utils.mapify(
                                                    "type", "pattern",
                                                    "pattern", "[\\-a-zA-Z0-9]+",
                                                    "message", "Идентификатор может содержать только символы a-z, A-Z, 0-9 и тире"
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
    private static InputForm createProblemForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "new_problem_name",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Имя нового задания",
                                            "placeholder", "Введите имя нового задания"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                    "type", "pattern",
                                                    "pattern", "[\\-a-zA-Z0-9_]+",
                                                    "message", "Идентификатор может содержать только символы a-z, A-Z, 0-9, тире и подчеркивание"
                                            )
                                            //TODO test such problem already exists
                                    )
                            ),
                            Utils.mapify(
                                    "name", "new_problem_type",
                                    "view", Utils.mapify(
                                            "type", "dropdown",
                                            "title", "Тип нового задания",
                                            "placeholder", "Выберите тип нового задания",
                                            "variants", SerializationTypesRegistry.PROBLEM.getTypes()
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
    private static InputForm createFolderForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "new_folder_name",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Имя нового подкаталога",
                                            "placeholder", "Введите имя нового подкаталога"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                    "type", "pattern",
                                                    "pattern", "[\\-a-zA-Z0-9_]+",
                                                    "message", "Идентификатор может содержать только символы a-z, A-Z, 0-9, тире и подчеркивание"
                                            )
                                            //TODO test such folder already exists
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

    public static InputForm getCloneEventForm() {
        return cloneEventForm;
    }

    public static InputForm getCopyEventForm() {
        return copyEventForm;
    }

    public static InputForm getCreateProblemForm() {
        return createProblemForm;
    }

    public static InputForm getCreateFolderForm() {
        return createFolderForm;
    }
}
