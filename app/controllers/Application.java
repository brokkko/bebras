package controllers;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.actions.AuthenticatedAction;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.User;
import models.Utils;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

@DcesController
public class Application extends Controller {

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
        return redirect(routes.Application.enter("bebras13"));
    }

    public static Result returnFile(String file) throws IOException {
        InputStream resource = Application.class.getResourceAsStream("/public/bebras-training/" + file);

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
}