package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.contests_list_domain;

@LoadEvent
@Authenticated(autoRegister = true)
@DcesController
public class DomainContests extends Controller {

    public static Result contests(String eventId) {
        return ok(contests_list_domain.render());
    }
}
