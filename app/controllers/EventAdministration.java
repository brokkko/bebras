package controllers;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.*;
import models.forms.InputForm;
import models.forms.RawForm;
import models.forms.validators.FileListValidator;
import models.newproblems.ProblemLink;
import models.newproblems.bbtc.BBTCProblemsLoader;
import models.newserialization.FormDeserializer;
import models.newserialization.FormSerializer;
import models.newserialization.JSONDeserializer;
import models.newserialization.MongoSerializer;
import models.results.Info;
import models.utils.Utils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import scala.concurrent.duration.Duration;
import views.html.contests_list;
import views.html.error;
import views.html.event_admin;
import views.htmlblocks.HtmlBlock;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.05.13
 * Time: 0:07
 */
@LoadEvent
@Authenticated(admin = true)
@DcesController
public class EventAdministration extends Controller {

    public static Result uploadKenguruSchoolCodes(String eventId) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart kenguruCodes = body.getFile("kenguru-codes");
        if (kenguruCodes == null)
            return badRequest(error.render("Не выбран файл для загрузки", new String[]{}));

        try {
            File file = kenguruCodes.getFile();
            File destFolder = Event.current().getEventDataFolder();
            destFolder = new File(destFolder, "keng-codes");

            destFolder.mkdir();

            File destFile = new File(destFolder, kenguruCodes.getFilename());
            Path destPath = Paths.get(destFile.getAbsolutePath());
            String kenguruCodesDest = FileListValidator.getKenguruSchoolsFile().getAbsolutePath();

            Files.copy(Paths.get(file.getAbsolutePath()), destPath, StandardCopyOption.REPLACE_EXISTING);
            Files.move(Paths.get(file.getAbsolutePath()), Paths.get(kenguruCodesDest), StandardCopyOption.REPLACE_EXISTING);

            return redirect(routes.EventAdministration.admin(Event.currentId()));
        } catch (IOException e) {
            Logger.error("Failed to make a file operation", e);
            return badRequest(error.render("Ошибка при загрузке файла, " + e.getMessage(), new String[]{}));
        }
    }

    public static Result uploadProblemsFile(String eventId, String contestId) {
        //TODO code duplication with uploadKenguruSchoolCodes

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart problemsFilePart = body.getFile("problem-set");
        if (problemsFilePart == null)
            return badRequest(error.render("Не выбран файл для загрузки", new String[]{}));

        try {
            File file = problemsFilePart.getFile();
//            Problems.removeLinksSubtree(eventId + '/' + contestId);
            new BBTCProblemsLoader().load(file, new ProblemLink(eventId).child(contestId));

            //move file
            File destFolder = new File(Event.current().getEventDataFolder(), "tasks-" + contestId);
            destFolder.mkdir();
            File destFile = new File(destFolder, problemsFilePart.getFilename());

            Files.move(Paths.get(file.getAbsolutePath()), Paths.get(destFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);

            return redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));
        } catch (IOException e) {
            Logger.error("Failed to make a file operation", e);
            return badRequest(error.render("Ошибка при загрузке файла, " + e.getMessage(), new String[]{}));
        }
    }

    @SuppressWarnings("UnusedParameters")
    public static Result admin(String eventId) {
        FormSerializer serializer = new FormSerializer(Forms.getEventChangeForm());
        Event event = Event.current();

        try {
            event.serialize(serializer);
        } catch (Exception e) {
            //do nothing
        }

        return ok(event_admin.render(serializer.getRawForm(), new RawForm()));
    }

    @SuppressWarnings("UnusedParameters")
    public static Result doChangeEvent(String eventId) {
        FormDeserializer deserializer = new FormDeserializer(Forms.getEventChangeForm());
        RawForm rawForm = deserializer.getRawForm();

        if (rawForm.hasErrors())
            return ok(event_admin.render(rawForm, new RawForm()));

        Event event = Event.current();
        event.updateFromEventChangeForm(deserializer);
        event.store();

        return redirect(routes.EventAdministration.admin(eventId));
    }

    public static Result addContest(String eventId) throws IOException {
        FormDeserializer formDeserializer = new FormDeserializer(Forms.getAddContestForm());
        RawForm rawForm = formDeserializer.getRawForm();

        if (rawForm.hasErrors())
            return ok(contests_list.render(rawForm));

        String newContestId = formDeserializer.readString("id");
        String newName = formDeserializer.readString("name");

        Event event = Event.current();

        //load contest pattern

        String contestJson = Utils.getResourceAsString("/bbtc_contest_pattern.json");

        contestJson = contestJson.replaceAll("%%%cid%%%", newContestId).replaceAll("%%%eid%%%", eventId).replaceAll("%%%cname%%%", newName);

        //parse json: (code duplication: TODO move to utils)
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory f = mapper.getJsonFactory();
        JsonParser parser = f.createJsonParser(contestJson);
        JsonNode tree = mapper.readTree(parser);

        event.addContest(new Contest(event, new JSONDeserializer((ObjectNode) tree)));

        event.store();

        return redirect(routes.UserInfo.contestsList(eventId));
    }

    public static Result setHtmlBlock(String event, String block) {
        InputForm form = Forms.getSetHtmlBlockForm();

        FormDeserializer deserializer = new FormDeserializer(form);

        RawForm rawForm = deserializer.getRawForm();
        if (rawForm.hasErrors())
            return badRequest("Failed to get html");

        HtmlBlock htmlBlock = HtmlBlock.load(event, block);
        htmlBlock.setHtml(rawForm.get("html"));

        Logger.info(rawForm.get("html"));

        return ok();
    }

    public static Result doClone(String eventId) {
        FormDeserializer deserializer = new FormDeserializer(Forms.getCloneEventForm());
        RawForm form = deserializer.getRawForm();
        if (form.hasErrors())
            return ok(views.html.event_admin.render(new RawForm(), form));

        String newEventId = form.get("new_event_id");

        //just copy db object
        DBCollection eventsCollection = MongoConnection.getEventsCollection();
        DBObject cloningEvent = eventsCollection.findOne(new BasicDBObject("_id", eventId));
        cloningEvent.put("_id", newEventId);

        //remove some staff from contests
        try {
            BasicDBList contests = (BasicDBList) cloningEvent.get("contests");
            for (Object contest : contests) {
                DBObject contestObj = (DBObject) contest;
                contestObj.removeField("pid2name");
                contestObj.removeField("blocks");
            }
        } catch (Exception ignored) {
        }

        eventsCollection.save(cloningEvent);

        //clone user db object
        MongoSerializer userSerializer = new MongoSerializer();
        User.current().serialize(userSerializer);
        DBObject userObject = userSerializer.getObject();
        userObject.put(User.FIELD_EVENT, newEventId);
        userObject.removeField("_id");
        userObject.put("_contests", new BasicDBObject());
        MongoConnection.getUsersCollection().save(userObject);

        Event.invalidateCache(newEventId);

//        return ok(views.html.event_admin.render(new RawForm(), new RawForm()));
        return redirect(routes.Registration.login(newEventId));
    }

    public static Result doRemoveEvent(String eventId) {
        //TODO remove corresponding collections

        MongoConnection.getEventsCollection().remove(new BasicDBObject("_id", eventId));

        Event.invalidateCache(eventId);

        return ok(views.html.message.render("Событие удалено", "Событие успешно удалено", new String[]{}));
    }

    public static Result doInvalidateEventResults(final String eventId) {
        F.Promise<Boolean> promiseOfVoid = Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        User.invalidateAllEventResults(Event.getInstance(eventId));
                        return true;
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean result) {
                                flash("message", "Event results successfully invalidated");
                                return redirect(routes.EventAdministration.admin(eventId));
                            }
                        }
                )
        );
    }

    public static Result doInvalidateContestsAndEventResults(final String eventId) {
        F.Promise<Boolean> promiseOfVoid = Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        User.invalidateAllResults(Event.getInstance(eventId));
                        return true;
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean result) {
                                flash("message", "All results successfully invalidated");
                                return redirect(routes.EventAdministration.admin(eventId));
                            }
                        }
                )
        );
    }

    public static Result doRemoveAllRegisteredByRole(String eventId, String roleId) { //TODO generalize for other roles
        Event event = Event.current();
        UserRole role = event.getRole(roleId);
        if (role.hasRight("event admin"))
            return forbidden();

        DBObject query = new BasicDBObject(User.FIELD_EVENT, eventId);
        query.put(User.FIELD_USER_ROLE, roleId);

        MongoConnection.getUsersCollection().remove(query);

        return redirect(routes.EventAdministration.admin(eventId));
    }

    public static Result doLoadUserFields(String eventId) {
        if (!User.currentRole().hasRight("event admin"))
            return Results.forbidden();

        Http.MultipartFormData body = Http.Context.current().request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart newFields = body.getFile("new-fields");
        if (newFields == null) {
            flash("fields-upload-error", "Не выбран файл для загрузки");
            return Results.redirect(routes.EventAdministration.admin(eventId));
        }

        String[] regime = body.asFormUrlEncoded().get("regime");
        if (regime == null || regime.length == 0 || regime[0] == null || regime[0].isEmpty()) {
            flash("fields-upload-error", "Не выбран режим загрузки");
            return Results.redirect(routes.EventAdministration.admin(eventId));
        }

        //possible regimes: create update create-update
        final boolean createNew = regime[0].startsWith("create");
        final boolean updateOld = regime[0].endsWith("update");

        final Event event = Event.current();

        File fieldsFile = newFields.getFile();
        try {
            //move file
            File destFolder = new File(Event.current().getEventDataFolder(), "user-data");
            destFolder.mkdir();
            final File destFile = new File(destFolder, newFields.getFilename());

            Files.move(Paths.get(fieldsFile.getAbsolutePath()), Paths.get(destFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);

            Akka.system().scheduler().scheduleOnce(
                    Duration.Zero(),
                    new Runnable() {
                        public void run() {
                            try {
                                loadFileWithNewUserFields(event, destFile, createNew, updateOld);
                            } catch (IOException e) {
                                Logger.error("Error while uploading users or users fields", e);
                            }
                        }
                    },
                    Akka.system().dispatcher()
            );

            flash("fields-upload-ok", "Данные загружены и будут обработаны");
            return redirect(routes.EventAdministration.admin(eventId));
        } catch (Exception e) {
            flash("fields-upload-error", "При загрузке данных произошла ошибка: " + e.getMessage());
            Logger.error("error loading file " + fieldsFile, e);
            return redirect(routes.EventAdministration.admin(eventId));
        }
    }

    private static void loadFileWithNewUserFields(Event event, File file, boolean createNew, boolean updateOld) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "windows-1251"), ';', '"');
        String[] title = reader.readNext();
        if (title == null)
            throw new IOException("No title in csv file");

        String transformation = getUserFieldsTransformation(title);
        title = transformUserFieldsTitle(title, transformation);

        //find _id index
        int idInd = findIndexInArray(title, "_id");

        System.out.println(idInd);

        String[] line;
        while ((line = reader.readNext()) != null) {
            line = transformUserFieldsLine(line, transformation);

            if (updateOld)
                updateUser(title, idInd, line);

            if (createNew)
                createUser(event, title, line);
        }
    }

    private static void updateUser(String[] title, int idInd, String[] line) {
        ObjectId id = new ObjectId(line[idInd]);

        User user = User.getInstance("_id", id);
        if (user == null) {
            Logger.warn("Failed to update user with id " + id);
            return; //TODO create new, if it is appropriate
        }

        for (int i = 0; i < title.length; i++)
            if (i != idInd)
                user.getInfo().put(title[i], line[i]);

        user.invalidateAllResults();
    }

    private static void createUser(Event event, String[] title, String[] line) {
        String password = "1234";
        User register = null;
        Info info = new Info();
        UserRole role = null;

        for (int i = 0; i < title.length; i++) {
            switch (title[i]) {
                case User.FIELD_REGISTERED_BY:
                    try {
                        register = User.getInstance("_id", new ObjectId(line[i]), event.getId());
                        if (register == null)
                            throw new IllegalArgumentException();
                    } catch (IllegalArgumentException e) {
                        Logger.warn("Failed to find user with id" + line[i]);
                        return;
                    }
                    break;
                case "password":
                    password = line[i];
                    break;
                case User.FIELD_USER_ROLE:
                    role = event.getRole(line[i]);
                    if (role == UserRole.EMPTY) {
                        Logger.warn("Failed to find role " + line[i]);
                        return;
                    }
                    break;
                default:
                    //TODO not very good. May be it is better to take InfoPattern from role
                    switch (line[i]) {
                        case "true":
                            info.put(title[i], true);
                            break;
                        case "false":
                            info.put(title[i], false);
                            break;
                        default:
                            info.put(title[i], line[i]);
                            break;
                    }
            }
        }

        event.createUser(password, role, info, register);
    }

    private static String getUserFieldsTransformation(String[] title) {
        if (findIndexInArray(title, "Ticher_f") >= 0)
            return "novosib_tichers";

        return null;
    }

    private static String[] transformUserFieldsTitle(String[] title, String transformation) {
        switch (transformation) {
            case "novosib_tichers":
                return new String[]{
                        "login",
                        "password",
                        "email",
                        "surname",
                        "name",
                        "patronymic",
                        "region",
                        "phone",
                        "school_name",
                        "index",
                        "address",
                        "knew_from",
                        "want_ad",
                        User.FIELD_REGISTERED_BY,
                        User.FIELD_USER_ROLE
                };
            default:
                return title;
        }
    }

    private static String[] transformUserFieldsLine(String[] line, String transformation) {
        switch (transformation) {
            case "novosib_tichers":
                //get name and patronymic
                String namePatro = line[charToIndex('K')].trim();
                int spPos = namePatro.indexOf(' ');
                String name = namePatro;
                String patro = "";
                if (spPos >= 0) {
                    name = namePatro.substring(0, spPos);
                    patro = namePatro.substring(spPos + 1);
                }

                //get region
                String regionName = line[charToIndex('D')].trim();
                String region = "";
                switch (regionName) {
                    case "Республика Хакасия":
                        region = "HAK";
                        break;
                    case "Республика Бурятия":
                        region = "BUR";
                        break;
                    case "Омская область":
                        region = "OMS";
                        break;
                    case "Новосибирская область":
                        region = "NVS";
                        break;
                    case "Магаданская область":
                        region = "MAG";
                        break;
                    case "Красноярский край":
                        region = "KRY";
                        break;
                    case "Иркутская область":
                        region = "IRK";
                        break;
                    case "Приморский край":
                        region = "PRI";
                        break;
                    default:
                        region = "UNKN";
                        Logger.warn("Unknown region "+ regionName);
                }

                return new String[] {
                        line[charToIndex('N')],
                        line[charToIndex('O')],
                        line[charToIndex('L')],
                        line[charToIndex('J')],
                        name,
                        patro,
                        region,
                        line[charToIndex('I')],
                        line[charToIndex('C')],
                        line[charToIndex('G')],
                        line[charToIndex('H')],
                        "От новосибиского рег.пр.",
                        "false",
                        "5249e727e4b045d4d8c3a841",
                        "SCHOOL_ORG"
                };
            default:
                return line;
        }
    }

    private static int findIndexInArray(String[] title, String field) {
        int idInd = -1;

        for (int i = 0; i < title.length; i++)
            if (title[i].equals(field)) {
                idInd = i;
                break;
            }

        return idInd;
    }

    private static int charToIndex(char c) {
        return c - 'A';
    }
}