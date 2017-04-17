package controllers.actions;

import models.Event;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import views.html.error;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 0:46
 */
public class LoadEventAction extends Action<LoadEvent> {
    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Event current = Event.current();

        if (current == Event.ERROR_EVENT) {
            Result ok = ok(error.render("actions.unknown_event", null));
            return F.Promise.pure(ok);
        }

        return delegate.call(ctx);
    }
}
