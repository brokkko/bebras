package controllers.actions;

import models.Contest;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import views.html.error;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 03.05.13
 * Time: 23:26
 */
public class LoadContestAction extends Action<LoadContest> {
    @Override
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Contest contest = Contest.current();

        if (contest == null) {
            SimpleResult ok = ok(error.render("actions.unknown_contest", null));
            return F.Promise.pure(ok);
        }

        return delegate.call(ctx);
    }
}

