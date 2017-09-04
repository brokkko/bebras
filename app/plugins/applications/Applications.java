package plugins.applications;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import controllers.worker.Worker;
import models.Event;
import models.User;
import models.UserRole;
import models.applications.Application;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.*;
import models.utils.Utils;
import org.bson.types.ObjectId;
import play.Logger;
import play.api.libs.iteratee.Cont;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import views.Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Results.*;

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
    private boolean showKvits = true;
    private List<ApplicationType> applicationTypes;
    private List<PaymentType> paymentTypes;

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
    public F.Promise<Result> doGet(String action, String params) {
        boolean level1 = User.currentRole().hasRight(right);
        boolean level2 = User.currentRole().hasRight(adminRight);

        for (PaymentType paymentType : paymentTypes) {
            F.Promise<Result> result = paymentType.processGetRequest(this, action, params, level1, level2);
            if (result != null)
                return result;
        }

        if (level1)
            updateSelfApplications();

        switch (action) {
            case "apps":
                if (level1)
                    return F.Promise.pure(organizerApplications());
                break;
            case "view-app":
                String[] splitParams = params.split("/");
                if (splitParams.length != 2)
                    return F.Promise.pure(badRequest());
                String userId = splitParams[0];
                String appName = splitParams[1];
                return F.Promise.pure(showApp(userId, appName));
        }

        return F.Promise.pure(Results.notFound());
    }

    @Override
    public F.Promise<Result> doPost(String action, String params) {
        boolean level1 = User.currentRole().hasRight(right);
        boolean level2 = User.currentRole().hasRight(adminRight);

        for (PaymentType paymentType : paymentTypes) {
            F.Promise<Result> result = paymentType.processPostRequest(this, action, params, level1, level2);
            if (result != null)
                return result;
        }

        if (!level1 && !level2)
            return F.Promise.pure(Results.forbidden());

        switch (action) {
            case "remove_app":
                return F.Promise.pure(removeApplication(params));
            case "add_app":
                return F.Promise.pure(addApplication());
            case "do_payment":
                return F.Promise.pure(doPayment(params));
            case "confirm_app":
                if (!level2)
                    return F.Promise.pure(Results.forbidden());
                return F.Promise.pure(confirmApplication(params));
            case "transfer":
                if (!level2)
                    return F.Promise.pure(Results.forbidden());
                return F.Promise.pure(transferApplications());
        }

        return F.Promise.pure(Controller.notFound());
    }

    @Override
    public boolean needsAuthorization() {
//        return true;
        return false;
    }

    public Application getApplicationByName(String name) {
        return getApplicationByName(name, User.current());
    }

    public Application getApplicationByName(String name, User user) {
        List<Application> applications = getApplications(user);

        for (Application application : applications)
            if (application.getName().equals(name))
                return application;

        return null;
    }

    public int getApplicationPrice(Application application) {
        return application.getSize() * getTypeByName(application.getType()).getPrice();
    }

    private Result addApplication() {
        User user = User.current();
        Event event = Event.current();

        List<Application> applications = getApplications(user);

        FormDeserializer deserializer = new FormDeserializer(getAddApplicationForm());
        RawForm rawForm = deserializer.getRawForm();
        if (rawForm.hasErrors())
            return ok(views.html.applications.org_apps.render(Event.current(), applications, rawForm, new RawForm(), this));

        String type = deserializer.readString("type");
        int size = deserializer.readInt("size");

        ApplicationType appType = getTypeByName(type);

        if (appType == null) {
            Logger.warn("Adding application with unknown type " + type);
            return badRequest();
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

        int state = Application.NEW;
        boolean noPay = appType.getPrice() == 0;
        if (noPay)
            state = Application.PAYED;

        Application newApplication = new Application(user, size, number, appType.getTypeName(), state);

        if (!appType.isNeedsConfirmation()) {
            newApplication.setState(Application.CONFIRMED);
            String participantRoleName = appType.getParticipantRole();
            if (participantRoleName != null) {
                UserRole participantRole = event.getRole(participantRoleName);
                if (participantRole == UserRole.EMPTY)
                    badRequest();

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

        doPayment(User.current(), application, comment);

        return result;
    }

    public void doPayment(User user, Application application, String comment) {
        if (application.getState() == Application.NEW) {
            application.setState(Application.PAYED);
            application.setComment(comment);
            user.store();
        }
    }

    private Result confirmApplication(String params) {
        RawForm form = new RawForm();
        form.bindFromRequest();
        String returnTo = form.get("-return-to");

        String[] userAndName = params.split("/");
        if (userAndName.length != 2)
            return badRequest();

        String userId = userAndName[0];
        String appName = userAndName[1];

        User user;
        try {
            user = User.getInstance("_id", new ObjectId(userId));
        } catch (IllegalArgumentException ignored) { //failed to instantiate Object id
            return badRequest();
        }

        Application application = getApplicationByName(appName, user);

        if (application == null)
            return Results.notFound();

        String confirmationResult = confirmApplication(Event.current(), user, User.current(), application);
        if (confirmationResult == null)
            return Results.redirect(returnTo);
        else
            return badRequest(confirmationResult);
    }

    //returns error string or null
    //application must be either PAYED or CONFIRMED
    public String confirmApplication(Event event, User user, User confirmingUser, Application application) {
        if (application == null)
            return "Unknown application";

        int state = application.getState();
        if (state == Application.NEW)
            return "Application must not have the state NEW";

        ApplicationType appType = getTypeByName(application.getType());
        if (appType == null)
            return "Unknown application type " + application.getType();

        int newState = state == Application.CONFIRMED ? Application.PAYED : Application.CONFIRMED;

        Logger.info(String.format(
                "Application %s of user %s (%s) changed to %s by %s (%s) (event %s)",
                application.getName(),
                user.getLogin(),
                user.getId().toString(),
                newState == Application.CONFIRMED ? "confirmed" : "payed",
                confirmingUser == null ? "[nobody]" : confirmingUser.getLogin(),
                confirmingUser == null ? "[nobody]" : confirmingUser.getId().toString(),
                event.getId()
        ));

        application.setState(newState);

        String participantRoleName = appType.getParticipantRole();

        //test that this applicationType has users
        if (participantRoleName != null) {
            UserRole participantRole = event.getRole(participantRoleName);
            if (participantRole == UserRole.EMPTY)
                return "unknown participant role " + participantRoleName;

            boolean usersManipulationResult;
            if (newState == Application.CONFIRMED)
                usersManipulationResult = application.createUsers(event, user, participantRole, appType);
            else
                usersManipulationResult = application.removeUsers(event);

            if (!usersManipulationResult)
                return "no manipulation was performed with users";
        }

        user.store();

        return null;
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

    private Result organizerApplications() {
        return organizerApplications(new RawForm(), new RawForm());
    }

    private Result organizerApplications(RawForm addForm, RawForm transferForm) {
        User user = User.current();

        List<Application> applications = getApplications(user);

        return ok(views.html.applications.org_apps.render(Event.current(), applications, addForm, transferForm, this));
    }

    public List<Application> getApplications(User user) { //TODO report: extract method does not extract //noinspection
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
                                                        "compare", "<=500"
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

    public boolean isShowKvits() {
        return showKvits;
    }

    public ApplicationType getTypeByName(String typeName) {
        for (ApplicationType applicationType : applicationTypes)
            if (applicationType.getTypeName().equals(typeName))
                return applicationType;

        return null;
    }

    public List<ApplicationType> getApplicationTypes() {
        return applicationTypes;
    }

    public List<PaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("user field", userField);
        serializer.write("right", right);
        serializer.write("admin right", adminRight);
        serializer.write("menu", menuTitle);
//        serializer.write("show kvits", showKvits);
        SerializationTypesRegistry.list(new SerializableSerializationType<>(ApplicationType.class)).write(serializer, "types", applicationTypes);
        SerializationTypesRegistry.list(new PaymentTypeSerializationType()).write(serializer, "payment types", paymentTypes);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        userField = deserializer.readString("user field", "apps");
        right = deserializer.readString("right", "school org");
        adminRight = deserializer.readString("admin right", "region org");
        menuTitle = deserializer.readString("menu", "Заявки");
        applicationTypes = SerializationTypesRegistry.list(new SerializableSerializationType<>(ApplicationType.class)).read(deserializer, "types");

        paymentTypes = SerializationTypesRegistry.list(new PaymentTypeSerializationType()).read(deserializer, "payment types");
        //legacy. If payment types are not specified, then add one depending on the legacy "show kvits" field:
        //true: add KvitBankTransferPaymentType, false: add SelfConfirmPaymentType
        if (paymentTypes.isEmpty()) {
            showKvits = deserializer.readBoolean("show kvits", true);
            if (showKvits)
                paymentTypes.add(new KvitBankTransferPaymentType());
            else
                paymentTypes.add(new SelfConfirmPaymentType());
        }
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
            return badRequest();

        final String destEventId = rawForm.get("dest_event");
        final Event destEvent = Event.getInstance(destEventId);

        if (destEvent == null)
            return badRequest("No destination event " + destEventId);

        final String destRoleName = rawForm.get("dest_role");
        final UserRole destRole = destEvent.getRole(destRoleName);
        if (destRole == UserRole.EMPTY)
            return badRequest("Unknown destination role");

        final Applications destPlugin = findApplicationPlugin(destEvent, type);

        if (destPlugin == null)
            return badRequest("no such app type in destination");

        final ApplicationType destAppType = destPlugin.getTypeByName(type);

        if (destAppType == null)
            return badRequest("destination event does not have corresponding type");

        final UserRole newParticipantRole = destEvent.getRole(destAppType.getParticipantRole());
        if (newParticipantRole == UserRole.EMPTY)
            return badRequest("invalid destination role");

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

                        userIndex++;
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

    private Result showApp(String userId, String appName) {
        User applicationUser;
        try {
            applicationUser = User.getUserById(new ObjectId(userId));
        } catch (IllegalArgumentException e) {
            return notFound("user not found");
        }

        if (applicationUser == null)
            return notFound("user not found");

        if (applicationUser.hasSameId(User.current()))
            return redirect(getAppsCall()); //TODO keep 'page-info' flash value

        Application application = getApplicationByName(appName, applicationUser);
        if (application == null)
            return notFound("application not found");

        ApplicationType type = getTypeByName(application.getType());
        if (type == null)
            return notFound("application type not found");

        if (!User.currentRole().hasRight(type.getRightToPay()))
            return forbidden();

        return ok(views.html.applications.view_app.render(this, application, applicationUser));
    }
}
