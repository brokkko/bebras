package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.actions.Authenticated;
import controllers.actions.AuthenticatedAction;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.*;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.*;
import models.results.InfoPattern;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.html.user_visits;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        if (User.currentRole() == UserRole.ANON)
            return redirect(routes.DomainContests.contests(eventId));
        else
            return ok(views.html.contests_list.render(new RawForm()));
    }

    public static boolean mayChange(User user, User userToChange) {
        return user.hasEventAdminRight() || user.isUpper(userToChange);
    }

    private static boolean mayChangeUserInfo() {
        Date userInfoChangeClosed = Event.current().getUserInfoChangeClosed();
        boolean mayChange = userInfoChangeClosed == null || userInfoChangeClosed.after(AuthenticatedAction.getRequestTime());
        if (User.current().hasEventAdminRight())
            mayChange = true;
        return mayChange;
    }

    @SuppressWarnings("UnusedParameters")
    public static Result info(String eventId, String userId) { //TODO use event id
        User user = User.current();

        ObjectId userIdAsObject;
        try {
            userIdAsObject = userId == null ? null : new ObjectId(userId);
        } catch (IllegalArgumentException ignored) {
            return Results.notFound();
        }
        User userToChange = userIdAsObject == null ? user : User.getUserById(userIdAsObject);

        if (userToChange == null)
            return badRequest();

        if (!mayChange(user, userToChange))
            return forbidden();

        FormSerializer formSerializer = new FormSerializer(userToChange.getRole().getEditUserForm());
        userToChange.serialize(formSerializer);

        return ok(views.html.user_info.render(
                userToChange,
                mayChangeUserInfo(),
                formSerializer.getRawForm(),
                userId != null,
                flash("user_info_change_msg")
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

        if (!mayChangeUserInfo()) {
            flash("user_info_change_msg", "Редактирование данных запрещено");
            return redirect(routes.UserInfo.info(eventId, oneUserChangesHimOrHerself ? null : userToChange.getId().toString()));
        }

        boolean partialRegistration = false;
        boolean wasPartial = userToChange.isPartialRegistration();

        if (form.hasErrors()) {
            if (oneUserChangesHimOrHerself || !formDeserializer.isPartiallyFilled()) // login and email are not changeable, thus no required
                return ok(views.html.user_info.render(userToChange, true, form, userId != null, null));
            else
                partialRegistration = true;
        }

        userToChange.updateFromForm(formDeserializer, registrationForm);
        userToChange.setPartialRegistration(partialRegistration);
        userToChange.store();

        flash("user_info_change_msg", "page.user_info.info_changed");

        return wasPartial && oneUserChangesHimOrHerself ?
                redirect(routes.Application.enter(eventId)) :
                redirect(routes.UserInfo.info(eventId, oneUserChangesHimOrHerself ? null : userToChange.getId().toString()));
    }

    public static Result showVisitInfo(String eventId, String userId) {
        User user = User.current();

        ObjectId userIdAsObject;
        try {
            userIdAsObject = new ObjectId(userId);
        } catch (IllegalArgumentException ignored) {
            return Results.notFound();
        }
        User userToView = User.getUserById(userIdAsObject);

        if (!mayChange(user, userToView)) //TODO may change is not the same as may view visits
            return forbidden();

        //search in users table
        List<UserActivityEntry> visits = new ArrayList<>();
        DBCursor allVisitsCursor = MongoConnection.getActivityCollection().find(
                new BasicDBObject(UserActivityEntry.FIELD_USER, userToView.getId())
        ).sort(
                new BasicDBObject(UserActivityEntry.FIELD_DATE, -1)
        );
        while (allVisitsCursor.hasNext()) {
            DBObject visitObject = allVisitsCursor.next();
            visits.add(UserActivityEntry.deserialize(new MongoDeserializer(visitObject)));
        }

        return ok(user_visits.render(userToView, visits));
    }

    public static Result removeUser(String eventId, String userId) {
        User adminUser = User.current();

        RawForm form = new RawForm();
        form.bindFromRequest();
        String returnTo = form.get("-return-to");

        User.removeUserById(
                Event.getInstance(eventId),
                new ObjectId(userId),
                adminUser.hasEventAdminRight() ? null : adminUser.getId()
        );

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
        user.getInfo().put(flag, currentValue == null ? "+" : null);
        user.store();

        return redirect(returnTo);
    }

    public static List<Contest> listOfContestsStartedByUser(User user) {
        List<Contest> contests = user.getEvent().getContestsAvailableForUser(user);
        List<Contest> startedContests = new ArrayList<>();
        for (Contest contest : contests)
            if (/*!contest.isAvailableForAnon() &&*/ user.getContestInfoCreateIfNeeded(contest.getId()).getStarted() != null)
                startedContests.add(contest);

        return startedContests;
    }

    public static Result changeRegisteredBy(String event, String user, String userBy) {
        if (!User.currentRole().hasEventAdminRight())
            return Results.forbidden("Do not do this again");

        User userLower = User.getUserByLogin(user);

        if (userLower == null)
            return Results.ok("failed to find first user");

        User userUpper = User.getUserByLogin(userBy);

        if (userUpper == null)
            return Results.ok("failed to find second user");

        if (!userUpper.getRole().mayRegister(userLower.getRole()))
            return Results.ok("User role " + userUpper.getRole().getName() + " can not register role " + userLower.getRole().getName());

        userLower.setRegisteredBy(userUpper);
        userLower.store();

        return Results.ok("User updated");
    }
}
