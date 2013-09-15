package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.actions.Authenticated;
import controllers.actions.AuthenticatedAction;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.*;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.FormDeserializer;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.Akka;
import play.libs.F;
import play.mvc.*;
import views.htmlblocks.HtmlBlock;

import java.io.*;
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
}