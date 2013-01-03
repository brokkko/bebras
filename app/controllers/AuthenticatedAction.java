package controllers;

import models.User;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 0:46
 */
public class AuthenticatedAction extends Action<Authenticated> {
    @Override
    public Result call(Http.Context ctx) throws Throwable {
        String userName = ctx.session().get("user");

        Result loginRedirect = redirect(routes.Application.index());

        if (userName == null)
            return loginRedirect; //TODO redirect to login

        ctx.request().setUsername(userName);

        if (configuration.load())
            if (User.current() == null)
                return loginRedirect;


        return delegate.call(ctx);
    }
}
