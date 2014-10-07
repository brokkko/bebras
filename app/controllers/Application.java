package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Event;
import models.ServerConfiguration;
import models.User;
import models.utils.Utils;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.event_message;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
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
    @Authenticated(redirectToLogin = false)
    public static Result enter(String eventId) {

        String url = request().uri();
        int pos = url.lastIndexOf("/enter");
        if (pos < 0)
            return notFound();

        return redirect(url.substring(0, pos) + "/" + User.currentRole().getEnterUrl());
    }

    public static Result root() {
        String defaultEvent = ServerConfiguration.getInstance().getCurrentDomain().getDefaultEvent();
        return redirect(routes.Application.enter(defaultEvent));
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
        String extension = Utils.getExtension(uploadingName);
        if (extension == null)
            return badRequest();
        extension = extension.toLowerCase();
        if (!imagesExtensions.contains(extension))
            return badRequest();

        File pictureFile = picture.getFile();
        File destFile = ServerConfiguration.getInstance().getNewResourceFile(extension);

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
        result.put("thumbUrl", routes.Resources.returnFile(destFile.getName()).toString());

        ArrayNode arrayOfResults = (ArrayNode) Json.parse("[]");
        arrayOfResults.add(result);
        return ok(arrayOfResults).as("text/html"); //text/html is because upload_image plugin needs this
    }

    @Authenticated
    @LoadEvent
    public static Result substituteUser(String eventId, String userIdAsString) {
        ObjectId userId;
        try {
            userId = new ObjectId(userIdAsString);
        } catch (IllegalArgumentException ignored) {
            return badRequest();
        }

        User user = User.current();
        User suUser = User.getUserById(userId);

        if (suUser == null)
            return badRequest();

        if (!user.hasEventAdminRight() && !user.getId().equals(suUser.getRegisteredBy()))
            return forbidden();

        session(User.getUsernameSessionKey(), suUser.getLogin());
        session(User.getSuUsernameSessionKey(), user.getLogin());

        return redirect(routes.Application.enter(eventId));
    }

    @LoadEvent
    @Authenticated(redirectToLogin = false)
    public static Result setSubscription(String eventId, String userId, boolean subscription) {
        ObjectId uid;
        try {
            uid = new ObjectId(userId);
        } catch (IllegalArgumentException ignored) {
            return notFound();
        }
        User user = User.getUserById(uid);
        if (user == null)
            return notFound();

        user.setWantAnnouncements(subscription);
        user.store();

        String text = subscription ? "Вы успешно подписались на рассылку" : "Вы успешно отписались от рассылки";
        text += " сообщений о событии " + Event.current().getTitle() + ".";
        if (!subscription)
            text += " Вернуть подписку вы можете в своем личном кабинете в разделе \"Личные данные\".";

        return ok(event_message.render("Изменение статуса подписки", text));
    }

    @Authenticated
    @LoadEvent
    public static Result substituteUserExit(String eventId) {
        String login = session(User.getSuUsernameSessionKey());
        if (login == null)
            return badRequest();
        session(User.getUsernameSessionKey(), login);
        session().remove(User.getSuUsernameSessionKey());

        return redirect(routes.Application.enter(eventId));
    }

    //regime = 'add' or 'remove'
    public static Result setupIpTracing(String ip, String regime) {
        boolean addIp = "add".equals(regime);
        ServerConfiguration config = ServerConfiguration.getInstance();
        if (addIp)
            config.addTraceIp(ip);
        else
            config.removeTraceIp(ip);

        return ok("ip status changed");
    }
}