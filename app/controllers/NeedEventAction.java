package controllers;

import models.Event;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 03.01.13
 * Time: 19:31
 */
public class NeedEventAction extends Action<NeedEvent> {

    public Result call(Http.Context ctx) throws Throwable {
        //TODO path is already parsed, but I don't know now how to extract eventId from the delegate
        String path = ctx.request().path();
        int firstPos = path.indexOf('/'); //should be 0

        Status status = badRequest("Unknown event");

        if (firstPos != 0)
            return status;

        int secondPos = path.indexOf('/', firstPos + 1);
        if (secondPos < 0)
            secondPos = path.length();

        String eventId = path.substring(firstPos + 1, secondPos);

        Event event = Event.getInstance(eventId);
        if (event == null)
            return status;

        ctx.args.put("event", event);

        return delegate.call(ctx);
    }
}
