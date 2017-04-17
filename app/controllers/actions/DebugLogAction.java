package controllers.actions;

import models.Event;
import play.Logger;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.01.13
 * Time: 15:17
 */
public class DebugLogAction extends Action<DebugLog> {
    @Override
    public F.Promise<Result> call(Http.Context context) throws Throwable {
        Http.Context.current.set(context);

        Event event = Event.current();

        if (event == null)
            return delegate.call(context);

        String title = event.getTitle();
        Logger.debug(title + ": " + configuration.value());

        return delegate.call(context);
    }
}
