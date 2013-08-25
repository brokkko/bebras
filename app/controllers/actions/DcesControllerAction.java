package controllers.actions;

import controllers.MongoConnection;
import models.Event;
import models.ServerConfiguration;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import plugins.Plugin;
import views.html.error;

/**
 * Created by ilya
 */
public class DcesControllerAction extends Action<DcesController> {

    @Override
    public Result call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        ServerConfiguration config = ServerConfiguration.getInstance();
        if (config.isMaintenanceMode())
            //TODO попытаться обойтись без времени обслуживания
            return ok(error.render("В данный момент сервер находится в режиме обслуживания, зайдите позже", null)); //TODO сделать время возвращения

        //migrate if needed
        if (config.getDbVersion() != ServerConfiguration.CURRENT_DB_VERSION && !ctx.request().uri().endsWith("/migrate"))
            return ok(error.render("В данный момент сервер находится в режиме обслуживания, зайдите позже", null)); //TODO избавиться и от этой хрени тоже

        //initialize plugins
        Event event = Event.current();
        if (event != Event.ERROR_EVENT)
            for (Plugin plugin : event.getPlugins())
                plugin.init();

        Result call = delegate.call(ctx);

        finalizeRequest(ctx);

        return call;
    }

    private void finalizeRequest(Http.Context ctx) {
        MongoConnection.storeEnqueuedUsers(ctx);
    }
}
