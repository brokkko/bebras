package controllers;

import controllers.actions.Authenticated;
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
@Authenticated
public class Plugins extends Controller {

    public static Result doGet(String event, String plugin, String action) {
        Plugin p = Event.current().getPlugin(plugin);
        if (p == null)
            return notFound();
        return p.doGet(action);
    }

    public static Result doPost(String event, String plugin, String action) {
        Plugin p = Event.current().getPlugin(plugin);
        if (p == null)
            return notFound();
        return p.doPost(action);
    }

}
