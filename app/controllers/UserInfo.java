package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.FormDeserializer;
import models.newserialization.FormSerializer;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.SerializationType;
import models.results.InfoPattern;
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

        if (userToChange == null)
            return badRequest();

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

        boolean oneUserChangesHimOrHerself = userToChange.getId().equals(user.getId());

        InputForm registrationForm = userToChange.getRole().getEditUserForm();

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm);

        RawForm form = formDeserializer.getRawForm();

        boolean partialRegistration = false;
        boolean wasPartial = userToChange.isPartialRegistration();

        if (form.hasErrors()) {
            if (oneUserChangesHimOrHerself || !formDeserializer.isPartiallyFilled()) // login and email are not changeable, thus no required
                return ok(views.html.user_info.render(userToChange, form, userId != null, null));
            else
                partialRegistration = true;
        }

        userToChange.updateFromForm(formDeserializer, registrationForm);
        userToChange.setPartialRegistration(partialRegistration);
        userToChange.store();

        flash("user_info_change_msg", "1");

        return wasPartial && oneUserChangesHimOrHerself ?
                       redirect(routes.Application.enter(eventId)) :
                       redirect(routes.UserInfo.info(eventId, oneUserChangesHimOrHerself ? null : userToChange.getId().toString()));
    }

    private static boolean mayChange(User user, User userToChange) {
        return user == userToChange || user.hasEventAdminRight() || user.getId().equals(userToChange.getRegisteredBy());
    }

    public static Result removeUser(String eventId, String userId) {
        User adminUser = User.current();

        RawForm form = new RawForm();
        form.bindFromRequest();
        String returnTo = form.get("-return-to");

        ObjectId user = new ObjectId(userId);
        DBObject query = new BasicDBObject("_id", user);

        if (!adminUser.hasEventAdminRight())
            query.put(User.FIELD_REGISTERED_BY, adminUser.getId());

        query.put("event_id", eventId); //for any case, ensure not to delete a user from another event
        MongoConnection.getUsersCollection().remove(query);
        return redirect(returnTo);
    }

    //TODO generalize to changing of any field
    //TODO generalize this user actions. This, for example, has some common code with remove User
    public static Result swapFlag(String eventId, String userIdString, String flag) {
        RawForm form = new RawForm();
        form.bindFromRequest();
        String returnTo = form.get("-return-to");

        ObjectId userId = new ObjectId(userIdString);
        User user = User.getInstance("_id", userId);

        InfoPattern infoPattern = user.getRole().getUserInfoPattern();
        SerializationType flagType = infoPattern.getType(flag);
        if (!(flagType instanceof BasicSerializationType) || !((BasicSerializationType) flagType).getClassName().equals("java.lang.String"))
            return badRequest();

        //TODO test admin rights

        if (!user.getEvent().getId().equals(eventId))
            return badRequest();

        String currentValue = (String) user.getInfo().get(flag);
        user.getInfo().put(flag, currentValue == null ? "yes" : null);
        user.store();

        return redirect(returnTo);
    }

}
