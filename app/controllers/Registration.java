package controllers;

import models.Event;
import models.MongoObject;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.register;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.01.13
 * Time: 16:39
 */
public class Registration extends Controller {

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

        user.put("event_id", event.getOid());
        user.store();

        return redirect(routes.Application.index());
    }

    public static Result waitForEmail(Event event) {
        Event.setCurrent(event);

//        return ok(views.html.);

        return null;
    }
}