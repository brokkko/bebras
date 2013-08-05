package controllers.actions;

import controllers.MongoConnection;
import models.ServerConfiguration;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import views.html.error;

/**
 * Created by ilya
 */
public class DcesControllerAction extends Action<DcesController> {

    @Override
    public Result call(Http.Context ctx) throws Throwable {
        if (ServerConfiguration.getInstance().isMaintenanceMode())
            //TODO попытаться обойтись без времени обслуживания
            return ok(error.render("В данный момент сервер находится в режиме обслуживания, зайдите позже", null)); //TODO сделать время возвращения

        Result call = delegate.call(ctx);

        finalizeRequest(ctx);

        return call;
    }

    private void finalizeRequest(Http.Context ctx) {
        MongoConnection.storeEnqueuedUsers(ctx);
    }
}
