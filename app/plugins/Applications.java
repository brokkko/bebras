package plugins;

import models.Event;
import models.User;
import models.UserRole;
import models.utils.Utils;
import models.applications.Application;
import models.applications.Kvit;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.*;
import org.bson.types.ObjectId;
import play.Logger;
import play.api.templates.Html;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.Menu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import controllers.routes;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.09.13
 * Time: 14:29
 */
public class Applications extends Plugin { //TODO test for right in all calls

    private String right = "school org";
    private String adminRight = "region org";
    private String userField = "apps";
    private List<ApplicationType> applicationTypes;

    @Override
    public void initPage() {
        Menu.addMenuItem("Заявки", getCall("apps"), right);
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(
                                            right,
                                            userField, //TODO take this field from plugin configuration
                                            SerializationTypesRegistry.list(new SerializableSerializationType<>(Application.class)),
                                            "Заявки"
        );
    }

    @Override
    public Result doGet(String action, String params) {
        boolean level1 = User.currentRole().hasRight(right);
        boolean level2 = User.currentRole().hasRight(adminRight);
        if (!level1 && !level2)
            return Results.forbidden();

        switch (action) {
            case "apps":
                return organizerApplications();

            case "kvit":
                if (!level1)
                    return Results.forbidden();
                return showKvit(params);

            case "pdfkvit":
                if (!level1)
                    return Results.forbidden();
                return showPdfKvit(params);

            case "kvit_example":
                if (!level2)
                    return Results.forbidden();
                return showKvit();
        }

        return Results.notFound();
    }

    @Override
    public Result doPost(String action, String params) {
        boolean level1 = User.currentRole().hasRight(right);
        boolean level2 = User.currentRole().hasRight(adminRight);
        if (!level1 && !level2)
            return Results.forbidden();

        switch (action) {
            case "remove_app":
                return removeApplication(params);
            case "add_app":
                return addApplication();
            case "do_payment":
                return doPayment(params);
            case "confirm_app":
                if (!level2)
                    return Results.forbidden();
                return confirmApplication(params);
        }

        return Controller.notFound();
    }

    @Override
    public boolean needsAuthorization() {
        return true;
    }

    private Application getApplicationByName(String name) {
        return getApplicationByName(name, User.current());
    }

    private Application getApplicationByName(String name, User user) {
        List<Application> applications = getApplications(user);

        for (Application application : applications)
            if (application.getName().equals(name))
                return application;

        return null;
    }

    public int getApplicationPrice(Application application) {
        return application.getSize() * getTypeByName(application.getType()).getPrice();
    }

    private Result showKvit() {
        User user = User.current();
        Kvit kvit = Kvit.getKvitFromUserDescription(user);

        if (kvit.isGenerated())
            return Results.ok(views.html.applications.kvit.render(null, this, kvit));
        else
            return Results.redirect(routes.Application.returnFile(kvit.getKvitFileName()));
    }

    private Result showKvit(String name) {
        User user = User.current();

        Kvit kvit = Kvit.getKvitForUser(user);
        Application application = getApplicationByName(name);
        if (application == null)
            return Controller.notFound();
        return Controller.ok(views.html.applications.kvit.render(application, this, kvit));
    }

    private Result showPdfKvit(String name) {
        //https://code.google.com/p/wkhtmltopdf
        //may need to install ubuntu fontconfig package

        final Application application = getApplicationByName(name);
        final Kvit kvit = Kvit.getKvitForUser(User.current());

        if (application == null)
            return Controller.notFound();

        F.Promise<File> promiseOfVoid = Akka.future(
                new Callable<File>() {
                    public File call() throws Exception {
                        return kvit.generatePdfKvit(Applications.this, application);
                    }
                }
        );

        return Controller.async(
                promiseOfVoid.map(
                        new F.Function<File, Result>() {
                            public Result apply(File file) {
                                Controller.response().setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
                                return Controller.ok(file).as("application/pdf");
                            }
                        }
                )
        );
    }

    private Result addApplication() {
        User user = User.current();
        Event event = Event.current();

        List<Application> applications = getApplications(user);

        FormDeserializer deserializer = new FormDeserializer(getAddApplicationForm());
        RawForm rawForm = deserializer.getRawForm();
        if (rawForm.hasErrors())
            return Controller.ok(views.html.applications.org_apps.render(Event.current(), applications, rawForm, this, Kvit.getKvitForUser(user)));

        int number = 1;
        int appsSize = applications.size();
        if (appsSize != 0)
            number = applications.get(appsSize - 1).getNumber() + 1;

        String type = deserializer.readString("type");
        ApplicationType appType = getTypeByName(type);
        if (appType == null) {
            Logger.warn("Adding application with unknown type " + type);
            return Results.badRequest();
        }

        Application newApplication = new Application(user, deserializer.readInt("size"), number, type);

        if (!appType.isNeedsConfirmation()) {
            newApplication.setState(Application.CONFIRMED);
            String participantRoleName = appType.getParticipantRole();
            if (participantRoleName != null) {
                UserRole participantRole = event.getRole(participantRoleName);
                if (participantRole == UserRole.EMPTY)
                    Controller.badRequest();

                newApplication.createUsers(event, user, participantRole);
            }
        }

        applications.add(newApplication);
        user.store();

        return Controller.redirect(getCall("apps"));
    }

    private Result doPayment(String name) {
        RawForm form = new RawForm();
        form.bindFromRequest();
        String comment = form.get("comment");

        Application application = getApplicationByName(name);
        if (application == null)
            return Controller.notFound();

        Result result = Controller.redirect(getCall("apps"));

        if (application.getState() != Application.NEW)
            return result;

        application.setState(Application.PAYED);
        application.setComment(comment);
        User.current().store();

        return result;
    }

    private Result confirmApplication(String params) {
        RawForm form = new RawForm();
        form.bindFromRequest();
        String returnTo = form.get("-return-to");

        String[] userAndName = params.split("/");
        if (userAndName.length != 2)
            return Controller.badRequest();

        String userId = userAndName[0];
        String appName = userAndName[1];

        User user;
        try {
            user = User.getInstance("_id", new ObjectId(userId));
        } catch (IllegalArgumentException ignored) { //failed to instantiate Object id
            return Controller.badRequest();
        }

        Application application = getApplicationByName(appName, user);

        if (application == null)
            return Results.notFound();

        int state = application.getState();
        if (state == Application.NEW)
            return Results.badRequest();

        ApplicationType appType = getTypeByName(application.getType());
        if (appType == null)
            return Results.badRequest();

        int newState = state == Application.CONFIRMED ? Application.PAYED : Application.CONFIRMED;
        application.setState(newState);

        Event event = Event.current();

        String participantRoleName = appType.getParticipantRole();

        //test that this applicationType has users
        if (participantRoleName != null) {
            UserRole participantRole = event.getRole(participantRoleName);
            if (participantRole == UserRole.EMPTY)
                Controller.badRequest();

            boolean usersManipulationResult;
            if (newState == Application.CONFIRMED)
                usersManipulationResult = application.createUsers(event, user, participantRole);
            else
                usersManipulationResult = application.removeUsers(event);

            if (!usersManipulationResult)
                return Results.internalServerError();
        }

        user.store();

        return Results.redirect(returnTo);
    }

    public boolean mayRemoveApplication(Application application) {
        ApplicationType applicationType = getTypeByName(application.getType());
        return applicationType == null || !applicationType.isNeedsConfirmation() || application.getState() != Application.CONFIRMED;
    }

    private Result removeApplication(String name) {
        User user = User.current();
        Event event = Event.current();

        List<Application> applications = getApplications(user);

        for (int i = 0; i < applications.size(); i++) {
            Application application = applications.get(i);
            if (application.getName().equals(name)) {
                application.removeUsers(event);
                applications.remove(i);
                user.store();

                return Controller.redirect(getCall("apps"));
            }
        }

        return Controller.notFound();
    }

    public Html getKvitHtml(User user, Application app, Kvit kvit) {
        ApplicationType appType = getTypeByName(app.getType());

        if (appType == null || appType.getPrice() == 0) //no confirmation
            return Html.apply("&nbsp;");

        if (kvit.isGenerated())
            return views.html.applications.type_generated.render(app, this, kvit);
        return views.html.applications.type_file.render(app, kvit);
    }

    private Result organizerApplications() {
        User user = User.current();
        Kvit kvit = Kvit.getKvitForUser(user);

        List<Application> applications = getApplications(user);

        return Controller.ok(views.html.applications.org_apps.render(Event.current(), applications, new RawForm(), this, kvit));
    }

    private List<Application> getApplications(User user) { //TODO report: extract method does not extract //noinspection
        //noinspection unchecked
        return (List<Application>) user.getInfo().get(userField);
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

    public Call getPdfKvitCall(String name) {
        return getCall("pdfkvit", true, name);
    }

    public Call getDoPayCall(String name) {
        return getCall("do_payment", false, name);
    }

    public InputForm getAddApplicationForm() {
        List<String> titlesList = new ArrayList<>();
        List<String> typesList = new ArrayList<>();

        for (ApplicationType applicationType : applicationTypes) {
            String description = applicationType.getDescription();

            if (applicationType.getPrice() > 0)
                description += " (" + applicationType.getPrice() + "р.)";
            else
                description += " (бесплатно)";

            titlesList.add(description);
            typesList.add(applicationType.getTypeName());
        }

        return InputForm.deserialize(
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
                                                                                                                   "name", "type",
                                                                                                                   "view", Utils.mapify(
                                                                                                                                               "type", "dropdown",
                                                                                                                                               "title", "Тип заявки",
                                                                                                                                               "placeholder", "Выберите тип",
                                                                                                                                               "titles", titlesList,
                                                                                                                                               "variants", typesList
                                                                                               ),
                                                                                                                   "required", true,
                                                                                                                   "validators", Utils.listify()
                                                                                               )
                                                                          ),
                                                                          "validators", Utils.listify()
                                            )
        );
    }

    public String getRight() {
        return right;
    }

    public String getUserField() {
        return userField;
    }

    public ApplicationType getTypeByName(String typeName) {
        for (ApplicationType applicationType : applicationTypes)
            if (applicationType.getTypeName().equals(typeName))
                return applicationType;

        return null;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("user field", userField);
        serializer.write("right", right);
        serializer.write("admin right", adminRight);
        SerializationTypesRegistry.list(new SerializableSerializationType<>(ApplicationType.class)).write(serializer, "types", applicationTypes);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        userField = deserializer.readString("user field", "apps");
        right = deserializer.readString("right", "school org");
        adminRight = deserializer.readString("admin right", "region org");
        applicationTypes = SerializationTypesRegistry.list(new SerializableSerializationType<>(ApplicationType.class)).read(deserializer, "types");
    }
}
