package controllers;

import controllers.actions.Authenticated;
import controllers.actions.LoadEvent;
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
public class UserInfo extends Controller {

    public static Result contestsList(String eventId) {
        return ok(views.html.error.render("NOT IMPLEMENTED", null));
    }

    public static Result info(String eventId) {
        return ok(views.html.error.render("NOT IMPLEMENTED", null));
    }

}
