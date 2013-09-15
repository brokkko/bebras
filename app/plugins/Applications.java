package plugins;

import models.Event;
import models.applications.Application;
import models.forms.RawForm;
import models.newserialization.SerializableSerializationType;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationTypesRegistry;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import views.Menu;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.09.13
 * Time: 14:29
 */
public class Applications extends Plugin {

    @Override
    public void initPage() {
        Menu.addMenuItem("Заявки", getCall("apps"), "school org");
        Menu.addMenuItem("Обзор заявок", getCall("view_apps"), "event admin");
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(
                "school org",
                "apps",
                SerializationTypesRegistry.list(new SerializableSerializationType<>(Application.class)),
                "Заявки"
        );
    }

    @Override
    public Result doGet(String action) {
        switch (action) {
            case "apps":
                return organizerApplications();
            case "view_apps":
                return adminApplications();
        }

        return Controller.notFound();
    }

    private Result adminApplications() {
        return null;
    }

    private Result organizerApplications() {
        return Controller.ok(views.html.applications.org_apps.render(Event.current(), null, new RawForm()));
    }

    @Override
    public Result doPost(String action) {
        return Controller.notFound();
    }
}
