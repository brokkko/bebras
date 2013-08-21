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
        ServerConfiguration config = ServerConfiguration.getInstance();
        if (config.isMaintenanceMode())
            //TODO попытаться обойтись без времени обслуживания
            return ok(error.render("В данный момент сервер находится в режиме обслуживания, зайдите позже", null)); //TODO сделать время возвращения

        //migrate if needed
        if (config.getDbVersion() != ServerConfiguration.CURRENT_DB_VERSION && !ctx.request().uri().endsWith("/migrate"))
            return ok(error.render("В данный момент сервер находится в режиме обслуживания, зайдите позже", null)); //TODO избавиться и от этой хрени тоже

        Result call = delegate.call(ctx);

        finalizeRequest(ctx);

        return call;
    }

    private void finalizeRequest(Http.Context ctx) {
        MongoConnection.storeEnqueuedUsers(ctx);
    }
}
