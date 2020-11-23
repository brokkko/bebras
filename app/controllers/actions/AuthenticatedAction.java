package controllers.actions;

import controllers.Email;
import controllers.routes;
import models.Event;
import models.User;
import models.newserialization.MemoryDeserializer;
import models.results.Info;
import org.apache.commons.mail.EmailException;
import org.bson.types.ObjectId;
import play.Logger;
import play.Play;
import play.api.mvc.Call;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Results;
import play.mvc.Result;

import java.util.Date;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 0:46
 */
public class AuthenticatedAction extends Action<Authenticated> {
    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Event event = Event.current();

        String userName = ctx.session().get(User.getUsernameSessionKey());

        //TODO include return back url
        F.Promise<Result> loginRedirect = F.Promise.pure(Results.redirect(routes.Registration.login(event.getId())));

        if (userName == null) {
            if (configuration.autoRegister()) {
                userName = autoRegisterUser();
                ctx.session().put(User.getUsernameSessionKey(), userName);
            }

            if (userName == null)
                return configuration.redirectToLogin() ? loginRedirect : delegate.call(ctx);
        }

        ctx.request().setUsername(userName);

        //store request time to determine contest status
        ctx.args.put("request time", new Date());

        if (configuration.load()) {
            User user = User.current();

            if (user == null) {//call to current loads a user. And also tests if there is such a user
                ctx.session().remove(User.getUsernameSessionKey());
                return loginRedirect;
            }

            final Info info = user.getInfo();
            if (info.containsKey("language")) {
                ctx.changeLang(String.valueOf(info.get("language")));
                ctx.args.put("user language was set", true);
            }

            if (configuration.admin() && !user.hasEventAdminRight())
                return loginRedirect;

            if (needRedirectToFullRegistration(user))
                return F.Promise.pure(Results.redirect(routes.UserInfo.info(Event.currentId(), null)));
        }

        return delegate.call(ctx);
    }

    /**
     * Registers user and returns his username
     * @return the username of a new user
     */
    private String autoRegisterUser() {
        ObjectId userId = new ObjectId();
        String login = userId.toString();
        String email = login + "@autoregistered";

        User user = User.deserialize(new MemoryDeserializer(
                "_id", login,
                User.FIELD_LOGIN, login,
                User.FIELD_EMAIL, email,
                User.FIELD_USER_ROLE, "ANON"
        ));

        user.setPasswordHash("");
        user.setWantAnnouncements(false);
        user.setConfirmed(true);

        user.serialize(); // is the same as store immediately

        return login;
    }

    public static Date getRequestTime() {
        return (Date) Http.Context.current().args.get("request time");
    }

    private boolean needRedirectToFullRegistration(User user) {
        if (user.isPartialRegistration()) { //redirect to entering info
            //Redirect for all calls except "get | show user info" and "post | change user info"
            Call userInfo = routes.UserInfo.info(Event.currentId(), null);
            Call userChangeInfo = routes.UserInfo.doChangeInfo(Event.currentId(), null);
            Call suExit = routes.Application.substituteUserExit(Event.currentId());
            Call logout = routes.Registration.logout(Event.currentId());

            if (!compareCallWithCurrent(userInfo) && !compareCallWithCurrent(userChangeInfo) && !compareCallWithCurrent(suExit) && !compareCallWithCurrent(logout))
                return true;
        }

        return false;
    }

    private static boolean compareCallWithCurrent(Call call) {
        Http.Request request = Http.Context.current().request();
        return request.method().equals(call.method()) && request.uri().equals(call.url()); //TODO not sure this works in all situations
    }

}
