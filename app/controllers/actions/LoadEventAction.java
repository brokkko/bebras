package controllers.actions;

import models.Event;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.01.13
 * Time: 0:46
 */
public class LoadEventAction extends Action<LoadEvent> {
    @Override
    public Result call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        Event current = Event.current();

        if (current == Event.ERROR_EVENT)
            return ok(views.html.error.render("actions.unknown_event", null));

        return delegate.call(ctx);
    }
}
