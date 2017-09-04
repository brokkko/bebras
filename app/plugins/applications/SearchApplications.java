package plugins.applications;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import models.Event;
import models.User;
import models.applications.Application;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import play.Logger;
import play.data.DynamicForm;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import views.Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static play.mvc.Results.internalServerError;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

public class SearchApplications extends Plugin {

    private String right;
    private String menuTitle;
    private List<String> rolesToSearch;

    @Override
    public void initPage() {
        Menu.addMenuItem(menuTitle, getCall(), right);
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public F.Promise<Result> doGet(String action, String params) {
        if (!User.currentRole().hasRight(right))
            return F.Promise.pure(Results.forbidden());

        return F.Promise.pure(ok(views.html.applications.search_application.render("", false, this)));
    }

    @Override
    public F.Promise<Result> doPost(String action, String params) {
        if (!User.currentRole().hasRight(right))
            return F.Promise.pure(Results.forbidden());

        DynamicForm form = new DynamicForm().bindFromRequest();
        String code = form.get("code");

        //search applications plugin, the application itself, the user
        Event event = Event.current();
        List<Applications> allAppsPlugins = event.getPlugins().stream()
                .filter(p -> p instanceof Applications)
                .map(p -> (Applications) p)
                .collect(Collectors.toList());

        Application application = null;
        Applications applications = null;
        User applicationUser = null;

        BasicDBObject query = new BasicDBObject("event_id", event.getId());
        BasicDBList dbListOfRoles = new BasicDBList();
        dbListOfRoles.addAll(rolesToSearch);
        query.put(User.FIELD_USER_ROLE, new BasicDBObject("$in", dbListOfRoles));

        try (User.UsersEnumeration allUsers = User.listUsers(query)) {
            users_outer_loop:
            while (allUsers.hasMoreElements()) {
                User user = allUsers.nextElement();
                for (Applications apps : allAppsPlugins) {
                    Application app = apps.getApplicationByName(code, user);
                    if (app != null) {
                       application = app;
                       applications = apps;
                       applicationUser = user;

                       break users_outer_loop;
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("failed to enumerate users", e);
            return F.Promise.pure(internalServerError("failed to enumerate users"));
        }

        if (application == null)
            return F.Promise.pure(ok(views.html.applications.search_application.render(code, true, this)));
        else
            return F.Promise.pure(redirect(applications.getViewAppCall(applicationUser, code)));
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("right", right);
        serializer.write("menu title", menuTitle);
        SerializationTypesRegistry
                .list(new BasicSerializationType<>(String.class))
                .write(serializer, "roles", rolesToSearch);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        right = deserializer.readString("right", "-");
        menuTitle = deserializer.readString("menu title");
        rolesToSearch = SerializationTypesRegistry
                .list(new BasicSerializationType<>(String.class))
                .read(deserializer, "roles");
    }
}
