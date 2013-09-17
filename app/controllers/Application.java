package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.actions.AuthenticatedAction;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.ServerConfiguration;
import models.User;
import models.Utils;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.list_events;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@DcesController
public class Application extends Controller {

    private static Set<String> imagesExtensions = new HashSet<>();

    static {
        imagesExtensions.add("png");
        imagesExtensions.add("jpg");
        imagesExtensions.add("jpeg");
        imagesExtensions.add("gif");
        imagesExtensions.add("bmp");
    }

    public static DBObject getEventTemplate(String eventId) throws IOException {
        String eventTemplate = Utils.getResourceAsString("/bbtc_event_pattern.json");
        eventTemplate = eventTemplate.replaceAll("%%%eid%%%", eventId);
        return (DBObject) JSON.parse(eventTemplate);
    }

    public static Result migrate() {
        F.Promise<Boolean> promiseOfVoid = Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        MongoConnection.migrate();

                        return true;
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean ignored) {
                                return ok("migration finished");
                            }
                        }
                )
        );

    }

    public static Result migrateByIndex(final Integer index) {
        F.Promise<Boolean> promiseOfVoid = Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        MongoConnection.migrate(index);

                        return true;
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean ignored) {
                                return ok("migration finished");
                            }
                        }
                )
        );

    }

    public static Result setGlobalHtmlBlock(String block) {
        return EventAdministration.setHtmlBlock("~global", block);
    }

    @LoadEvent
    public static Result enter(String eventId) {
        AuthenticatedAction.doAuthenticate(); //TODO may be moved to DCES controller action

        String url = request().uri();
        int pos = url.lastIndexOf("/enter");
        if (pos < 0)
            return notFound();

        return redirect(url.substring(0, pos) + "/" + User.currentRole().getEnterUrl());
    }

    public static Result root() {
        String defaultEvent = ServerConfiguration.getInstance().getDefaultDomainEvent();
        return redirect(routes.Application.enter(defaultEvent));
    }

    public static Result returnResource(String file, String base) throws IOException {
        InputStream resource = Application.class.getResourceAsStream(base + "/" + file);

        if (resource == null)
            return notFound();

        String content = "text/plain";
        if (file.endsWith(".html"))
            content = "text/html";
        else if (file.endsWith(".css"))
            content = "text/css";
        else if (file.endsWith(".js"))
            content = "text/javascript";
        else if (file.endsWith(".png"))
            content = "image/png";

        return ok(resource).as(content);
    }

    public static Result returnFile(String file, String base) throws IOException {
        return ok(new File(Play.application().getFile(base).getAbsolutePath() + "/" + file));
    }

    public static Result wymEditorUpload() {
        //TODO has problems with cyrillic file names, and may have serious problems with html symbols in file name
        ObjectNode result = Json.newObject();
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("uploadedfile");

        if (picture == null)
            return badRequest();

//        String[] size = body.asFormUrlEncoded().get("thumbnailSize");

        //test extension
        String uploadingName = picture.getFilename();
        int dotPos = uploadingName.lastIndexOf('.');
        if (dotPos < 0)
            return badRequest();
        String extension = uploadingName.substring(dotPos + 1).toLowerCase();
        if (!imagesExtensions.contains(extension))
            return badRequest();

        File pictureFile = picture.getFile();
        String destFileName = ServerConfiguration.getInstance().getRandomString(20) + System.currentTimeMillis() + '.' + extension;
        File destFile = new File(ServerConfiguration.getInstance().getResourcesFolder(), destFileName);

        try {
            Files.move(
                    Paths.get(pictureFile.getAbsolutePath()),
                    Paths.get(destFile.getAbsolutePath())
            );
        } catch (Exception e) {
            Logger.error("Failed to move uploaded image", e);
            return internalServerError();
        }

        result.put("original_filename", "an image");
        result.put("thumbUrl", routes.Application.returnFile(destFileName).toString());

        ArrayNode arrayOfResults = (ArrayNode) Json.parse("[]");
        arrayOfResults.add(result);
        return ok(arrayOfResults).as("text/html"); //text/html is because upload_image plugin needs this
    }

    public static Result listEvents() {
        String domain = ServerConfiguration.getInstance().getCurrentDomain();

        List<String> events = new ArrayList<>();

        try (DBCursor eventsCursor = MongoConnection.getEventsCollection().find(
                new BasicDBObject("domain", domain),
                new BasicDBObject("_id", 1)
        )) {
            while (eventsCursor.hasNext()) {
                DBObject event = eventsCursor.next();
                events.add((String) event.get("_id"));
            }
        }

        return ok(list_events.render(events));
    }
}