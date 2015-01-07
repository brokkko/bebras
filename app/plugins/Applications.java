package plugins;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import controllers.routes;
import controllers.worker.Worker;
import models.Event;
import models.User;
import models.UserRole;
import models.applications.Application;
import models.applications.Kvit;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.*;
import models.utils.Utils;
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
import java.util.Map;
import java.util.concurrent.Callable;

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
    private String menuTitle = "Заявки";
    private List<ApplicationType> applicationTypes;

    @Override
    public void initPage() {
        Menu.addMenuItem(menuTitle, getCall("apps"), right);
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(
                right,
                userField,
                SerializationTypesRegistry.list(new SerializableSerializationType<>(Application.class)),
                "Заявки"
        );
    }

    @Override
    public Result doGet(String action, String params) {
        //simple action without authorization
        if ("pdfkvit".equals(action) && "example".equals(params))
            return showPdfKvit(params);
        if ("kvit_example".equals(action))
            return showKvit();

        boolean level1 = User.currentRole().hasRight(right);
        boolean level2 = User.currentRole().hasRight(adminRight);
        if (!level1 && !level2)
            return Results.forbidden();

        updateSelfApplications();

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
            case "transfer":
                if (!level2)
                    return Results.forbidden();
                return transferApplications();
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
        Kvit kvit = Kvit.getKvitForUser(user);

        if (kvit.isGenerated())
            return Results.ok(views.html.applications.kvit.render(null, this, kvit));
        else
            return Results.redirect(routes.Resources.returnFile(kvit.getKvitFileName()));
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

        final Application application = "example".equals(name) ?
                getExampleApplication() :
                getApplicationByName(name);
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

    private Application getExampleApplication() {
        return new Application(User.current(), 100, 1, applicationTypes.get(0).getTypeName());
    }

    private Result addApplication() {
        User user = User.current();
        Event event = Event.current();

        List<Application> applications = getApplications(user);

        FormDeserializer deserializer = new FormDeserializer(getAddApplicationForm());
        RawForm rawForm = deserializer.getRawForm();
        if (rawForm.hasErrors())
            return Controller.ok(views.html.applications.org_apps.render(Event.current(), applications, rawForm, new RawForm(), this, Kvit.getKvitForUser(user)));

        String type = deserializer.readString("type");
        int size = deserializer.readInt("size");

        ApplicationType appType = getTypeByName(type);

        if (appType == null) {
            Logger.warn("Adding application with unknown type " + type);
            return Results.badRequest();
        }

        addApplicationForUser(user, event, size, appType);

        return Controller.redirect(getAppsCall());
    }

    private Application addApplicationForUser(User user, Event event, int size, ApplicationType appType) {
        List<Application> applications = getApplications(user);
        int number = 1;
        int appsSize = applications.size();
        if (appsSize != 0)
            number = applications.get(appsSize - 1).getNumber() + 1;

        Application newApplication = new Application(user, size, number, appType.getTypeName());

        if (!appType.isNeedsConfirmation()) {
            newApplication.setState(Application.CONFIRMED);
            String participantRoleName = appType.getParticipantRole();
            if (participantRoleName != null) {
                UserRole participantRole = event.getRole(participantRoleName);
                if (participantRole == UserRole.EMPTY)
                    Controller.badRequest();

                newApplication.createUsers(event, user, participantRole, appType);
            }
        }

        applications.add(newApplication);
        user.store();

        return newApplication;
    }

    private Result doPayment(String name) {
        RawForm form = new RawForm();
        form.bindFromRequest();
        String comment = form.get("comment");

        Application application = getApplicationByName(name);
        if (application == null)
            return Controller.notFound();

        Result result = Controller.redirect(getAppsCall());

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

        User currentUser = User.current();
        Logger.info(String.format(
                "Application %s of user %s (%s) changed to %s by %s (%s) (event %s)",
                appName,
                user.getLogin(),
                user.getId().toString(),
                newState == Application.CONFIRMED ? "confirmed" : "payed",
                currentUser == null ? "[nobody]" : currentUser.getLogin(),
                currentUser == null ? "[nobody]" : currentUser.getId().toString(),
                Event.currentId()
        ));

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
                usersManipulationResult = application.createUsers(event, user, participantRole, appType);
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

        if (applicationType == null)
            return true;

        //noinspection SimplifiableIfStatement
        if (applicationType.isSelf())
            return false;

        return !applicationType.isNeedsConfirmation() || application.getState() != Application.CONFIRMED;

        //TODO report should find null
//        return !applicationType.isSelf() && (applicationType == null || !applicationType.isNeedsConfirmation() || application.getState() != Application.CONFIRMED);
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

                return Results.redirect(getAppsCall());
            }
        }

        return Results.notFound();
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
        return organizerApplications(new RawForm(), new RawForm());
    }

    private Result organizerApplications(RawForm addForm, RawForm transferForm) {
        User user = User.current();
        Kvit kvit = Kvit.getKvitForUser(user);

        List<Application> applications = getApplications(user);

        return Results.ok(views.html.applications.org_apps.render(Event.current(), applications, addForm, transferForm, this, kvit));
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

    public Call getTransferApplicationCall() {
        return getCall("transfer", false, "");
    }

    public boolean needApplicationForm() {
        for (ApplicationType applicationType : applicationTypes)
            if (!applicationType.isSelf())
                return true;

        return false;
    }

    public InputForm getAddApplicationForm() {
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
                                getAppTypeDropdown()
                        ),
                        "validators", Utils.listify()
                )
        );
    }

    public InputForm getApplicationTransferForm() {
        return InputForm.deserialize(
                new MemoryDeserializer(
                        "fields",
                        Utils.listify(
                                getAppTypeDropdown(),
                                Utils.mapify(
                                        "name", "dest_event",
                                        "view", Utils.mapify(
                                                "type", "string",
                                                "title", "Идентификатор события",
                                                "placeholder", "Введите id события"
                                        ),
                                        "required", true,
                                        "validators", Utils.listify()
                                ),
                                Utils.mapify( //TODO don't do type if there is only one application type
                                        "name", "dest_role",
                                        "view", Utils.mapify(
                                                "type", "string",
                                                "title", "Роль получателей",
                                                "placeholder", "Введите роль получателей"
                                        ),
                                        "required", true,
                                        "validators", Utils.listify()
                                )
                        ),
                        "validators", Utils.listify()
                )
        );

    }

    private Map<Object, Object> getAppTypeDropdown() {
        List<String> titlesList = new ArrayList<>();
        List<String> typesList = new ArrayList<>();

        for (ApplicationType applicationType : applicationTypes) {
            if (applicationType.isSelf())
                continue;
            if (!applicationType.isAllowSelect())
                continue;

            String description = applicationType.getDescription();

            if (applicationType.getPrice() > 0)
                description += " (" + applicationType.getPrice() + "р.)";
            else
                description += " (бесплатно)";

            titlesList.add(description);
            typesList.add(applicationType.getTypeName());
        }

        return Utils.mapify( //TODO don't do type if there is only one application type
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
        serializer.write("menu", menuTitle);
        SerializationTypesRegistry.list(new SerializableSerializationType<>(ApplicationType.class)).write(serializer, "types", applicationTypes);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        userField = deserializer.readString("user field", "apps");
        right = deserializer.readString("right", "school org");
        adminRight = deserializer.readString("admin right", "region org");
        menuTitle = deserializer.readString("menu", "Заявки");
        applicationTypes = SerializationTypesRegistry.list(new SerializableSerializationType<>(ApplicationType.class)).read(deserializer, "types");
    }

    public void updateSelfApplications() {
        User user = User.current();
        List<Application> applications = getApplications(user);
        for (ApplicationType applicationType : applicationTypes)
            if (applicationType.isSelf()) {
                //find applications of that type

                boolean hasApp = false;
                for (Application application : applications)
                    if (application.getType().equals(applicationType.getTypeName())) {
                        hasApp = true;
                        break;
                    }

                if (!hasApp)
                    addApplicationForUser(user, Event.current(), 1, applicationType);
            }
    }

    private Result transferApplications() {
        FormDeserializer deserializer = new FormDeserializer(getApplicationTransferForm());
        RawForm rawForm = deserializer.getRawForm();

        if (rawForm.hasErrors())
            return organizerApplications(new RawForm(), rawForm);

        final Event event = Event.current();
        final String type = rawForm.get("type");
        final ApplicationType appType = getTypeByName(type);

        if (appType == null)
            return Results.badRequest();

        final String destEventId = rawForm.get("dest_event");
        final Event destEvent = Event.getInstance(destEventId);

        if (destEvent == null)
            return Results.badRequest("No destination event " + destEventId);

        final String destRoleName = rawForm.get("dest_role");
        final UserRole destRole = destEvent.getRole(destRoleName);
        if (destRole == UserRole.EMPTY)
            return Results.badRequest("Unknown destination role");

        final Applications destPlugin = findApplicationPlugin(destEvent, type);

        if (destPlugin == null)
            return Results.badRequest("no such app type in destination");

        final ApplicationType destAppType = destPlugin.getTypeByName(type);

        if (destAppType == null)
            return Results.badRequest("destination event does not have corresponding type");

        final UserRole newParticipantRole = destEvent.getRole(destAppType.getParticipantRole());
        if (newParticipantRole == UserRole.EMPTY)
            return Results.badRequest("invalid destination role");

        final Worker worker = new Worker("transfer apps", "From event " + Event.currentId() + " to " + destEventId + ", type " + type);
        worker.execute(new Worker.Task() {
            @Override
            public void run() throws Exception {
                //begin transfer of applications

                DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId()); //TODO rewrite with Users.listUsers

                int userIndex = 0;
                int transferredUsersCount = 0;
                try (DBCursor usersCursor = MongoConnection.getUsersCollection().find(query)) {
                    while (usersCursor.hasNext()) {

                        User user = User.deserialize(new MongoDeserializer(usersCursor.next()));

                        if (!user.getRole().hasRight(right))
                            continue;

                        userIndex ++;
                        if (userIndex % 100 == 0)
                            worker.logInfo("processing user " + userIndex + " total transferred " + transferredUsersCount);

                        List<Application> applications = getApplications(user);
                        if (applications == null || applications.isEmpty())
                            continue;

                        List<Application> appsToTransfer = new ArrayList<>();
                        for (Application application : applications)
                            if (type.equals(application.getType()) && application.getState() == Application.CONFIRMED)
                                appsToTransfer.add(application);

                        if (appsToTransfer.isEmpty())
                            continue;

                        User newUser;
                        List<Application> transferredApplications;

                        User userWithSameLogin = User.getUserByLogin(destEventId, user.getLogin());
                        if (userWithSameLogin != null) {
                            if (user.getEmail().toLowerCase().equals(userWithSameLogin.getEmail().toLowerCase()))
                                worker.logWarn("User with this login already exists, and emails equal: " + userWithSameLogin.getLogin());
                            else {
                                worker.logWarn("User with this login already exists, but emails differ: " + userWithSameLogin.getLogin());
                                continue;
                            }

                            transferredApplications = new ArrayList<>();
                            for (Application application : appsToTransfer) {
                                Application transferredApplication = addApplicationForUser(userWithSameLogin, destEvent, application.getSize(), destAppType);
                                transferredApplications.add(transferredApplication);
                            }

                            newUser = userWithSameLogin;
                        } else {
                            newUser = transferUser(user, appsToTransfer);
                            transferredApplications = getApplications(newUser);
                        }

                        //remove registered users from applications
                        for (Application application : transferredApplications) {
                            application.clearUsers();
                            application.setState(Application.CONFIRMED);
                            application.createUsers(destEvent, newUser, newParticipantRole, destAppType);
                            worker.logInfo("Transferred user: (" + user.getLogin() + ") " + application.getName());
                        }

                        newUser.store();

                        transferredUsersCount++;
                    }
                }

                worker.logInfo("finished transfer");
            }

            private User transferUser(User user, List<Application> appsToTransfer) {
                User newUser = new User();

                newUser.setEvent(destEvent);
                newUser.setRole(destRole);
                newUser.setInfo(user.getInfo());
                newUser.setPasswordHash(user.getPasswordHash());
                newUser.setConfirmed(true);
                newUser.setPartialRegistration(true);
                newUser.setWantAnnouncements(true);

                //set apps
                newUser.getInfo().put(getUserField(), appsToTransfer);

                newUser.store();

                //reload user because we need to make new objects for its applications
                newUser = User.getUserByLogin(destEventId, user.getLogin());

                return newUser;
            }
        });

        return Results.redirect(getCall("apps"));
    }

    private Applications findApplicationPlugin(Event destEvent, String type) {
        for (Plugin plugin : destEvent.getPlugins())
            if (plugin instanceof Applications)
                for (ApplicationType applicationType : ((Applications) plugin).applicationTypes)
                    if (type.equals(applicationType.getTypeName()))
                        return (Applications) plugin;

        return null;
    }
}
