package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.actions.DcesController;
import models.Event;
import models.User;
import models.UserType;
import models.Utils;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.FormDeserializer;
import play.Play;
import play.cache.Cache;
import play.libs.Akka;
import play.libs.F;
import play.mvc.*;

import java.io.*;
import java.util.concurrent.Callable;

@DcesController
public class Application extends Controller {

    public static Result initialize() throws IOException {
        DBCollection bbtcCollection = MongoConnection.getEventsCollection();

        DBObject bbtcObject = getEventTemplate();
        bbtcCollection.save(bbtcObject);
        Cache.set("event-bbtc", null, 1);

        return ok("Site is successfully initialized");
    }

    public static Result initializeTrash() throws IOException {
        DBCollection configCollection = MongoConnection.getConfigCollection();
        if (configCollection.findOne() != null && !Play.isDev())
            return badRequest("Site is already initialized");
//            return notFound();

        //TODO this is a temporary initialization, move it somewhere else

        configCollection.remove(new BasicDBObject()); //remove all

        DBCollection bbtcCollection = MongoConnection.getEventsCollection();
//        bbtcCollection.remove(new BasicDBObject());

        //bbtc event object
        DBObject bbtcObject = getEventTemplate();
        bbtcCollection.save(bbtcObject);
        //clear Cache
        Cache.set("event-bbtc", null, 1);

        //bbtc test event object
        DBObject bbtcTestObject = (DBObject) JSON.parse(Utils.getResourceAsString("/bbtc_test_event.json"));
        bbtcCollection.save(bbtcTestObject);
        //clear Cache
        Cache.set("event-bbtc-test", null, 1);

        //save configuration object to make it not possible to reinit the application
        //configCollection.save(new BasicDBObject());
        return ok("Site is successfully initialized");
    }

    public static Result createEvent(String eventId) throws IOException {
        DBCollection eventsCollection = MongoConnection.getEventsCollection();

        //bbtc pattern event object
        DBObject bbtcObject = getEventTemplate();
        bbtcObject.put("_id", eventId);

        eventsCollection.save(bbtcObject);
        //clear Cache
        Cache.set("event-" + eventId, null, 1);

        //create admins
        Event event = Event.getInstance(eventId);

        createUser(event, "iposov", "zxcvvgcv42", "iposov@gmail.com", true);
        createUser(event, "mikhail_larionov", "letmeinplease", "mikhail_larionov@rambler.ru", true);

        return ok("Event " + eventId + " successfully initialized");
    }

    public static DBObject getEventTemplate() throws IOException {
        return (DBObject) JSON.parse(Utils.getResourceAsString("/bbtc_event.json"));
    }

    public static void createUser(Event event, String login, String password, String email, boolean isAdmin) {
        User.removeUser(event, login);

        InputForm registrationForm = Event.current().getUsersForm();

        RawForm myRawForm = new RawForm();

        myRawForm.put("login", login);
        myRawForm.put("surname", "noname");
        myRawForm.put("name", "noname");
        myRawForm.put("patronymic", "noname");
        myRawForm.put("email", email);
        myRawForm.put("schoolcode", "00000000");
        myRawForm.put("schoolname", "noname");
        myRawForm.put("personal_data", "1");
        myRawForm.put("contest_rules", "1");

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm, myRawForm);
//        RawForm rawForm = formDeserializer.getRawForm();
//        Logger.error("" + rawForm);

        User user = User.deserialize(formDeserializer);

        if (isAdmin)
            user.setType(UserType.EVENT_ADMIN);

        user.setConfirmed(true);
        user.setPasswordHash(User.passwordHash(password));

        user.setEvent(event);
        user.setPasswordHash(User.passwordHash(password));

        user.store();
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

}