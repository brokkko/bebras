package controllers;

import controllers.actions.Authenticated;
import controllers.actions.LoadEvent;
import models.Event;
import models.User;
import models.serialization.FormDeserializer;
import models.serialization.FormSerializer;
import models.forms.InputForm;
import models.forms.RawForm;
import play.mvc.Controller;

import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.01.13
 * Time: 0:59
 */
@LoadEvent
@Authenticated
public class UserInfo extends Controller {

    public static Result contestsList(String eventId) { //TODO use event id
        return ok(views.html.contests_list.render());
    }

    public static Result info(String eventId) { //TODO use event id
        User user = User.current();

        FormSerializer formSerializer = new FormSerializer(Event.current().getEditUserForm());
        user.store(formSerializer);

        return ok(views.html.user_info.render(
                formSerializer.getRawForm(),
                flash("user_info_change_msg") != null ? "page.user_info.info_changed" : null
        ));
    }

    public static Result doChangeInfo(String eventId) {
        InputForm registrationForm = Event.current().getEditUserForm();

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm);

        RawForm form = formDeserializer.getRawForm();

        if (form.hasErrors())
            return ok(views.html.user_info.render(form, null));

        User user = User.current();
        user.update(formDeserializer);
        user.store();

        flash("user_info_change_msg", "1");
        return redirect(routes.UserInfo.info(eventId));
    }

}
