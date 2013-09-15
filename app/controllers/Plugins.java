package controllers;

import controllers.actions.Authenticated;
import controllers.actions.AuthenticatedAction;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Event;
import play.mvc.Controller;
import play.mvc.Result;
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

    public static Result doGet(String event, String plugin, String action, String params) {
        AuthenticatedAction.doAuthenticate();

        Plugin p = Event.current().getPlugin(plugin);
        if (p == null)
            return notFound();
        return p.doGet(action, normalize(params));
    }

    public static Result doPost(String event, String plugin, String action, String params) {
        AuthenticatedAction.doAuthenticate();

        Plugin p = Event.current().getPlugin(plugin);
        if (p == null)
            return notFound();
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
