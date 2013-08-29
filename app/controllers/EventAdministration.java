package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import models.*;
import models.data.*;
import models.forms.InputForm;
import models.forms.RawForm;
import models.forms.validators.FileListValidator;
import models.newproblems.ProblemLink;
import models.newproblems.bbtc.BBTCProblemsLoader;
import models.newserialization.FormDeserializer;
import models.newserialization.FormSerializer;
import models.newserialization.JSONDeserializer;
import models.newserialization.MongoSerializer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.contests_list;
import views.html.error;
import views.html.event_admin;
import views.htmlblocks.HtmlBlock;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

//            String fileName = kenguruCodes.getFilename();
//            String contentType = kenguruCodes.getContentType();
        try {
            File file = kenguruCodes.getFile();
            Path destPath = Paths.get(FileListValidator.getKenguruSchoolsFile().toURI());

            Files.move(Paths.get(file.toURI()), destPath, StandardCopyOption.REPLACE_EXISTING);
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
            new BBTCProblemsLoader().load(file, new ProblemLink(eventId).child(contestId));
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

    // information about event

    public static <T> Result evalCsvTable(final String fileName, final TableDescription<T> tableDescription) {
        F.Promise<byte[]> promiseOfVoid = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws Exception {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        try (
                                ObjectsProvider<T> objectsProvider = tableDescription.getObjectsProviderFactory().get();
                                ZipOutputStream zos = new ZipOutputStream(baos);
                                CsvDataWriter<T> dataWriter = new CsvDataWriter<>(tableDescription.getTable(), zos, "windows-1251", ';', '"')
                        ) {
                            zos.putNextEntry(new ZipEntry(fileName + ".csv"));
                            dataWriter.writeObjects(objectsProvider);
                        }

                        return baos.toByteArray();
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                //TODO file name should be encode somehow http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    @SuppressWarnings("UnusedParameters")
    public static Result csvTable(final String eventId, final Integer tableIndex) throws Exception {
        TableDescription tableDescription = Event.current().getTable(tableIndex);

        if (tableDescription == null)
            return notFound("table not found");

        return evalCsvTable("table" + tableIndex + "-" + eventId, tableDescription);
    }

    @SuppressWarnings("UnusedParameters")
    @LoadContest
    public static Result csvTableForContest(final String eventId, final String contestId, final Integer tableIndex) throws Exception {
        TableDescription tableDescription = Contest.current().getTable(tableIndex);

        if (tableDescription == null)
            return notFound("table not found");

        return evalCsvTable("table" + tableIndex + "-" + eventId + "-" + contestId, tableDescription);
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
        eventsCollection.save(cloningEvent);

        //clone user db object
        MongoSerializer userSerializer = new MongoSerializer();
        User.current().serialize(userSerializer);
        DBObject userObject = userSerializer.getObject();
        userObject.put(User.FIELD_EVENT, newEventId);
        userObject.removeField("_id");
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
}