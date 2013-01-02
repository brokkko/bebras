package controllers;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import models.MongoObject;
import models.fields.InputForm;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.*;

import views.html.*;
import views.html.helper.form;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result registration() {
        DynamicForm dyn = new DynamicForm();
        //TODO in AS variable may be local - used in for
        /*dyn.reject(new ValidationError(""));
        ValidationError error = new ValidationError("field1", "msg.first", new ArrayList<Object>() {{
            add("42");
            add(239);
        }});

        dyn.reject(error);

        String msg = dyn.error("field1").message();

        return ok(msg).as("text/html");*/

        String formConfig =
                "{\"fields\": [" +
                        "{\"name\": \"login\", \"input\":{\"type\":\"string\", \"required\":true, \"placeholder\":\"Имя пользователя\"}}," +
                        "{\"name\": \"password\", \"input\":{\"type\":\"password\", \"required\":true, \"placeholder\":\"Пароль\"}}," +
                        "{\"name\": \"info\", \"input\":{\"type\":\"multiline\", \"required\":true, \"placeholder\":\"Дополнительные данные\"}}" +
                "]}";
        InputForm form = new InputForm(new MongoObject("no-collection", (DBObject) JSON.parse(formConfig)));

        return ok(form.format(dyn));
    }

}