package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import models.Event;
import models.MongoObject;
import models.fields.InputForm;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.*;

import views.html.*;
import views.html.helper.form;

import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result registration(String eventId) {
        DynamicForm dyn = new DynamicForm();
        Event e = Event.getInstance(eventId);
        if (e == null)
            return notFound("no such event");
        else
            return ok(register.render(e, e.getUsersForm(), dyn));
    }

    public static Result initialize() {
        DBCollection configCollection = MongoConnection.getConfigCollection();
        if (configCollection.findOne() != null)
            return badRequest("Site is already initialized");

        DBObject bbtcObject = new BasicDBObject();

        bbtcObject.put("event_id", "bbtc");
        bbtcObject.put("title", "Соревнование Английский Бульдог");

        String usersFormsConfig =
                "{\"fields\": [" +
                        "{\"name\": \"login\", \"input\":{\"type\":\"string\", \"required\":true, \"placeholder\":\"Имя пользователя\"}}," +
                        "{\"name\": \"password\", \"input\":{\"type\":\"password\", \"required\":true, \"placeholder\":\"Пароль\"}}," +
                        "{\"name\": \"info\", \"input\":{\"type\":\"multiline\", \"required\":true, \"placeholder\":\"Дополнительные данные\"}}" +
                        "]}";
        DBObject usersObject = (DBObject) JSON.parse(usersFormsConfig);

        bbtcObject.put("users", usersObject);
        bbtcObject.put("contests", Collections.emptyList());

        DBCollection bbtcCollection = MongoConnection.getEventsCollection();
        bbtcCollection.save(bbtcObject);

        configCollection.save(new BasicDBObject());
        return ok("Site is successfully initialized");
    }
}