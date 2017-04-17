package controllers.actions;

import models.Contest;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import views.html.error;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 03.05.13
 * Time: 23:26
 */
public class LoadContestAction extends Action<LoadContest> {
    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Contest contest = Contest.current();

        if (contest == null) {
            Result ok = ok(error.render("actions.unknown_contest", null));
            return F.Promise.pure(ok);
        }

        return delegate.call(ctx);
    }
}

