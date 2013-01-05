package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import models.Event;
import models.MongoObject;
import org.apache.commons.mail.EmailException;
import play.Play;
import play.cache.Cache;
import play.data.DynamicForm;
import play.mvc.*;

import views.html.*;

import java.io.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    //@NeedEvent
    public static Result registration(Event event) {
        //TODO use form.errorsAsJson() to ajax validate form

        Event.setCurrent(event);

        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        return ok(register.render(form));
        //TODO it is (almost) impossible to set breakpoint if there is a link to template
    }

    public static Result doRegistration(Event event) {
        Event.setCurrent(event);

        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        MongoObject user = new MongoObject(MongoConnection.getUsersCollection().getName()); //TODO no need to get all the collection
        event.getUsersForm().getObject(user, form);

        if (form.hasErrors())
            return ok(register.render(form));
//            return redirect(routes.Application.registration(event));

        user.put("event_id", event.getOid());
        user.store();

        return redirect(routes.Application.index());
    }

    public static Result initialize() throws IOException {
        DBCollection configCollection = MongoConnection.getConfigCollection();
        if (configCollection.findOne() != null && ! Play.isDev())
            return badRequest("Site is already initialized");
//            return notFound();
//        TODO bug - wrong indent after comments

        configCollection.remove(new BasicDBObject()); //remove all

        DBCollection bbtcCollection = MongoConnection.getEventsCollection();
        bbtcCollection.remove(new BasicDBObject());

        //bbtc event object
        DBObject bbtcObject = (DBObject) JSON.parse(getResourceAsString("/bbtc_event.json"));
        bbtcCollection.save(bbtcObject);

        //clear Cache
        Cache.set("event-bbtc", null, 1);

        //save configuration object to make it not possible to reinit the application
        configCollection.save(new BasicDBObject());
        return ok("Site is successfully initialized");
    }

    private static String getResourceAsString(String name) throws IOException {
        InputStream inS = Application.class.getResourceAsStream(name);
        BufferedReader inR = new BufferedReader(new InputStreamReader(inS, "UTF8"));
        CharArrayWriter out = new CharArrayWriter();
        int r;
        while ((r = inR.read()) >= 0)
            out.write(r);
        inR.close();
        return out.toString();
    }

    public static Result sendEmailTest() throws EmailException {
        return ok(Email.sendEmailTest("iposov@gmail.com", "test", "test test http://ya.ru"));
    }
}