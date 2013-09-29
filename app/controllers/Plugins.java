package controllers;

import controllers.actions.Authenticated;
import controllers.actions.AuthenticatedAction;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Event;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.08.13
 * Time: 14:37
 */
@DcesController
@LoadEvent
public class Plugins extends Controller {

    @Authenticated(redirectToLogin = false)
    public static Result doGet(String eventId, String plugin, String action, String params) {
        Plugin p = Event.current().getPlugin(plugin);
        if (p == null)
            return notFound();

        if (p.needsAuthorization() && !User.isAuthorized())
            return Results.redirect(routes.Registration.login(eventId));

        return p.doGet(action, normalize(params));
    }

    @Authenticated(redirectToLogin = false)
    public static Result doPost(String eventId, String plugin, String action, String params) {
        Plugin p = Event.current().getPlugin(plugin);
        if (p == null)
            return notFound();

        if (p.needsAuthorization() && !User.isAuthorized())
            return Results.redirect(routes.Registration.login(eventId));

        return p.doPost(action, normalize(params));
    }

    private static String normalize(String params) {
        if (params == null)
            return "";
        if (params.endsWith("/"))
            return params.substring(0, params.length() - 1);
        return params;
    }

}
