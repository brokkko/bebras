package controllers.actions;

import models.Contest;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 03.05.13
 * Time: 23:26
 */
public class LoadContestAction extends Action<LoadContest> {
    @Override
    public Result call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Contest contest = Contest.current();

        if (contest == null)
            return ok(views.html.error.render("actions.unknown_contest", null));

        return delegate.call(ctx);
    }
}

