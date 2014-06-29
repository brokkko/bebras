package controllers.actions;

import models.Event;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import views.html.error;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 0:46
 */
public class LoadEventAction extends Action<LoadEvent> {
    @Override
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Event current = Event.current();

        if (current == Event.ERROR_EVENT) {
            SimpleResult ok = ok(error.render("actions.unknown_event", null));
            return F.Promise.pure(ok);
        }

        return delegate.call(ctx);
    }
}
