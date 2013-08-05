package controllers.actions;

import controllers.routes;
import models.Event;
import models.User;
import models.UserType;
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
            return loginRedirect;

        ctx.request().setUsername(userName);

        //store request time to determine contest status
        ctx.args.put("request time", new Date());

        if (configuration.load()) {
            if (User.current() == null) //call to current loads user. And also test if there is such user
                return loginRedirect;

            if (configuration.admin() && User.current().getType() != UserType.EVENT_ADMIN)
                return loginRedirect;
        }

        return delegate.call(ctx);
    }

    public static Date getRequestTime() {
        return (Date) Http.Context.current().args.get("request time");
    }

}
