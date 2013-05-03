package controllers;

import controllers.actions.Authenticated;
import controllers.actions.LoadEvent;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 03.05.13
 * Time: 1:17
 */
@LoadEvent
@Authenticated
public class Contests extends Controller {

    public static Result startContest(String eventId, String contestId) {
        return TODO;
    }

    public static Result contest(String eventId, String contestId) {
        return TODO;
    }

}
