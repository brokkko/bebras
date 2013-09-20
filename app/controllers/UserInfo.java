package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.User;
import models.newserialization.FormDeserializer;
import models.newserialization.FormSerializer;
import models.forms.InputForm;
import models.forms.RawForm;
import org.bson.types.ObjectId;
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
    public static Result info(String eventId, String userId) { //TODO use event id
        User user = User.current();
        User userToChange = userId == null ? user : User.getInstance("_id", new ObjectId(userId)); //TODO wrong id leads to an exception
        if (!mayChange(user, userToChange))
            return forbidden();

        FormSerializer formSerializer = new FormSerializer(userToChange.getRole().getEditUserForm());
        userToChange.serialize(formSerializer);

        return ok(views.html.user_info.render(
                userToChange,
                formSerializer.getRawForm(),
                userId != null,
                flash("user_info_change_msg") != null ? "page.user_info.info_changed" : null
        ));
    }

    public static Result doChangeInfo(String eventId, String userId) {
        User user = User.current();
        User userToChange = userId == null ? user : User.getInstance("_id", new ObjectId(userId)); //TODO wrong id leads to an exception
        if (!mayChange(user, userToChange))
            return forbidden();

        InputForm registrationForm = userToChange.getRole().getEditUserForm();

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm);

        RawForm form = formDeserializer.getRawForm();

        if (form.hasErrors())
            return ok(views.html.user_info.render(userToChange, form, userId != null, null));

        userToChange.updateFromForm(formDeserializer, registrationForm);
        userToChange.store();

        flash("user_info_change_msg", "1");
        return redirect(routes.UserInfo.info(eventId, userToChange.getId().equals(user.getId()) ? null : userToChange.getId().toString()));
    }

    private static boolean mayChange(User user, User userToChange) {
        return user == userToChange || user.hasEventAdminRight() || userToChange.getRegisteredBy().equals(user.getId());
    }

}
