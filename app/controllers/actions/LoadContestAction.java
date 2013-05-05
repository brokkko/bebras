package controllers.actions;

import models.Contest;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;

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

        ctx.args.put("request time", new Date());

        return delegate.call(ctx);
    }

    public static Date getRequestTime() {
        return (Date) Http.Context.current().args.get("request time");
    }
}

