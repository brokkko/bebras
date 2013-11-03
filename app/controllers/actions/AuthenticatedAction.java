package controllers.actions;

import controllers.routes;
import models.Event;
import models.User;
import play.api.mvc.Call;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 0:46
 */
public class AuthenticatedAction extends Action<Authenticated> {
    @Override
    public Result call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Event event = Event.current();

        String userName = ctx.session().get(User.getUsernameSessionKey());

        //TODO include return back url
        Result loginRedirect = Results.redirect(routes.Registration.login(event.getId()));

        if (userName == null)
            return configuration.redirectToLogin() ? loginRedirect : delegate.call(ctx);

        ctx.request().setUsername(userName);

        //store request time to determine contest status
        ctx.args.put("request time", new Date());

        if (configuration.load()) {
            User user = User.current();

            if (user == null) //call to current loads a user. And also tests if there is such a user
                return loginRedirect;

            if (configuration.admin() && !user.hasEventAdminRight())
                return loginRedirect;

            if (needRedirectToFullRegistration(user))
                return Results.redirect(routes.UserInfo.info(Event.currentId(), null));
        }

        return delegate.call(ctx);
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
