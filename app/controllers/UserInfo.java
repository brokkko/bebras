package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Event;
import models.User;
import models.newserialization.FormDeserializer;
import models.newserialization.FormSerializer;
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
@DcesController
public class UserInfo extends Controller {

    @SuppressWarnings("UnusedParameters")
    public static Result contestsList(String eventId) {
        return ok(views.html.contests_list.render(new RawForm()));
    }

    @SuppressWarnings("UnusedParameters")
    public static Result info(String eventId) { //TODO use event id
        User user = User.current();

        FormSerializer formSerializer = new FormSerializer(Event.current().getRole("PARTICIPANT").getEditUserForm());
        user.serialize(formSerializer);

        return ok(views.html.user_info.render(
                formSerializer.getRawForm(),
                flash("user_info_change_msg") != null ? "page.user_info.info_changed" : null
        ));
    }

    public static Result doChangeInfo(String eventId) {
        InputForm registrationForm = Event.current().getRole("PARTICIPANT").getEditUserForm();

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm);

        RawForm form = formDeserializer.getRawForm();

        if (form.hasErrors())
            return ok(views.html.user_info.render(form, null));

        User user = User.current();
        user.updateFromForm(formDeserializer, registrationForm);
        user.store();

        flash("user_info_change_msg", "1");
        return redirect(routes.UserInfo.info(eventId));
    }

}
