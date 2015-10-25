package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import models.Contest;
import models.User;
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

    //starts contest if it is not already started, and otherwise starts contest
    @LoadContest
    public static Result goDomainContest(String eventId, String contestId) {
        User user = User.current();

        Contest contest = Contest.current();

//        if (user.contestFinished(contest))
//            user.
        return null;
    }
}
