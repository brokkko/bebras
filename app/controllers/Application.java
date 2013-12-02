package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Event;
import models.ServerConfiguration;
import models.User;
import models.utils.Utils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.cache.Cache;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.event_message;
import views.html.list_events;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
        String defaultEvent = ServerConfiguration.getInstance().getDefaultDomainEvent();
        return redirect(routes.Application.enter(defaultEvent));
    }

    public static Result returnResource(final String file, final String base) throws IOException {
        InputStream resource = Application.class.getResourceAsStream(base + "/" + file);

        if (resource == null)
            return notFound();

        String content = determineContentType(file);

        return ok(resource).as(content);
    }

    private static String determineContentType(String fileName) {
        fileName = fileName.toLowerCase();

        String content = "text/plain";

        if (fileName.endsWith(".html"))
            content = "text/html";
        else if (fileName.endsWith(".css"))
            content = "text/css";
        else if (fileName.endsWith(".js"))
            content = "text/javascript";
        else if (fileName.endsWith(".png"))
            content = "image/png";
        else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            content = "image/jpeg";
        else if (fileName.endsWith(".doc"))
            content = "application/msword";
        else if (fileName.endsWith(".csv"))
            content = "text/csv";
        else if (fileName.endsWith(".pdf"))
            content = "application/pdf";

        return content;
    }

    public static Result returnFile(String file) throws IOException {
//        String s = "abc";
//        for (int i = 0; i < 30000000; i++)
//            s = s.substring(1) + "a";

        String cacheKey = "resource-file-" + file;

        final String decodedFile = URLDecoder.decode(file, "UTF-8");

        byte[] content = null;
        try {
            content = Cache.getOrElse(cacheKey, new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    File resource = ServerConfiguration.getInstance().getResource(decodedFile);
                    if (!resource.exists())
                        return null;

                    //read file to byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024 * 8];
                    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(resource))) {
                        int read;
                        while ((read = in.read(buffer)) > 0)
                            baos.write(buffer, 0, read);
                    } catch (Exception e) {
                        Logger.warn("Failed to read resource " + decodedFile, e);
                        return null;
                    }

                    return baos.toByteArray();
                }
            }, 30 * 60);  //30 minutes
        } catch (Exception ignored) {
        }

        if (content == null)
            return notFound();

        String contentType = determineContentType(file);
        return ok(content).as(contentType);
    }

    @Authenticated(admin = true)
    @LoadEvent
    public static Result returnDataFile(String eventId, String file) throws UnsupportedEncodingException {
        file = URLDecoder.decode(file, "UTF-8");

        File content = new File(Event.current().getEventDataFolder().getAbsolutePath() + "/" + file);

        if (!content.exists())
            return notFound();

        return ok(content);
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
        result.put("thumbUrl", routes.Application.returnFile(destFile.getName()).toString());

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