package controllers;

import controllers.actions.Authenticated;
import controllers.actions.LoadEvent;
import models.Event;
import models.MongoObject;
import models.User;
import models.forms.InputForm;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;

import play.mvc.Result;
import views.html.register;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.01.13
 * Time: 0:59
 */
@LoadEvent
@Authenticated
public class UserInfo extends Controller {

    public static Result contestsList(String eventId) {
        return ok(views.html.contests_list.render());
    }

    public static Result info(String eventId) {
        User user = User.current();
        DynamicForm userForm = new DynamicForm();
        Event.current().getEditUserForm().fillForm(userForm, user.getStoredObject());

        return ok(views.html.user_info.render(
                userForm,
                flash("user_info_change_msg") != null ? "page.user_info.info_changed" : null
        ));
    }

    public static Result doChangeInfo(String eventId) {
        User user = User.current();

        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();
        InputForm registrationForm = Event.current().getEditUserForm();

        InputForm.FilledInputForm filledForm = registrationForm.validate(form);

        if (form.hasErrors())
            return ok(views.html.user_info.render(form, null));

        filledForm.fillObject(user.getStoredObject());

        user.getStoredObject().store();

        flash("user_info_change_msg", "1");
        return redirect(routes.UserInfo.info(eventId));
    }

}
