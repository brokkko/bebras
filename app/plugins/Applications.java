package plugins;

import models.Event;
import models.User;
import models.Utils;
import models.applications.Application;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.FormDeserializer;
import models.newserialization.MemoryDeserializer;
import models.newserialization.SerializableSerializationType;
import models.newserialization.SerializationTypesRegistry;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import views.Menu;
import views.html.kvit;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.09.13
 * Time: 14:29
 */
public class Applications extends Plugin {

    private final String RIGHT = "school org";

    @SuppressWarnings("unchecked")
    private static InputForm addApplicationForm = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "size",
                                        "view", Utils.mapify(
                                        "type", "int",
                                        "title", "Количество участников",
                                        "placeholder", "Введите количество участников"
                                    ),
                                    "required", true,
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                    "type", "int",
                                                    "compare", "<=100"
                                            ),
                                            Utils.mapify(
                                                    "type", "int",
                                                    "compare", ">0"
                                            )
                                    )
                            ),
                            Utils.mapify(
                                    "name", "kio",
                                    "view", Utils.mapify(
                                            "type", "dropdown",
                                            "title", "Тип заявки",
                                            "placeholder", "Выбереите тип",
                                            "titles", Utils.listify(
                                                "Конкурс Бобёр (50 р.)",
                                                "Конкурсы Бобёр и КИО (100 р.)"
                                            ),
                                            "variants", Utils.listify("b", "bk")
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

    @Override
    public void initPage() {
        Menu.addMenuItem("Заявки", getCall("apps"), RIGHT);
        Menu.addMenuItem("Обзор заявок", getCall("view_apps"), "event admin");
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(
                RIGHT,
                "apps",
                SerializationTypesRegistry.list(new SerializableSerializationType<>(Application.class)),
                "Заявки"
        );
    }

    @Override
    public Result doGet(String action, String params) {
        switch (action) {
            case "apps":
                return organizerApplications();
            case "view_apps":
                return adminApplications();
            case "kvit":
                return showKvit(params);
        }

        return Controller.notFound();
    }

    private Result showKvit(String name) {
        List<Application> applications = getApplications(User.current());

        for (Application application : applications)
            if (application.getName().equals(name))
                return Controller.ok(kvit.render(application, this));

        return Controller.notFound();
    }

    @Override
    public Result doPost(String action, String params) {
        switch (action) {
            case "remove_app":
                return removeApplication(params);
            case "add_app":
                return addApplication();
        }

        return Controller.notFound();
    }

    private Result addApplication() {
        User user = User.current();

        if (!user.hasRight(RIGHT))
            return Controller.forbidden();

        List<Application> applications = getApplications(user);

        FormDeserializer deserializer = new FormDeserializer(getAddApplicationForm());
        RawForm rawForm = deserializer.getRawForm();
        if (rawForm.hasErrors())
            return Controller.ok(views.html.applications.org_apps.render(Event.current(), applications, rawForm, this));

        int number = 1;
        int appsSize = applications.size();
        if (appsSize != 0)
            number = applications.get(appsSize - 1).getNumber() + 1;

        applications.add(new Application(user, deserializer.readInt("size"), number, "bk".equals(deserializer.readString("kio"))));
        user.store();

        return Controller.redirect(getCall("apps"));
    }

    private Result removeApplication(String name) {
        User user = User.current();

        if (!user.hasRight(RIGHT))
            return Controller.forbidden();

        List<Application> applications = getApplications(user);

        for (int i = 0; i < applications.size(); i++) {
            Application application = applications.get(i);
            if (application.getName().equals(name)) {
                applications.remove(i);
                user.store();

                return Controller.redirect(getCall("apps"));
            }
        }

        return Controller.notFound();
    }

    private Result adminApplications() {
        return Controller.ok("пока не готово");
    }

    private Result organizerApplications() {
        User user = User.current();

        if (!user.hasRight(RIGHT))
            return Controller.forbidden();

        List<Application> applications = getApplications(user);
        return Controller.ok(views.html.applications.org_apps.render(Event.current(), applications, new RawForm(), this));
    }

    private List<Application> getApplications(User user) { //TODO report: extract method does not extract //noinspection
        //noinspection unchecked
        return (List<Application>) user.getInfo().get("apps");
    }

    public Call getRemoveCall(String appName) {
        return getCall("remove_app", false, appName);
    }

    public Call getAddCall() {
        return getCall("add_app", false, "");
    }

    public Call getAppsCall() {
        return getCall("apps");
    }

    public Call getKvitCall(String name) {
        return getCall("kvit", true, name);
    }

    public static InputForm getAddApplicationForm() {
        return addApplicationForm;
    }
}
