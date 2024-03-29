package controllers.actions;

import controllers.MongoConnection;
import models.Event;
import models.ServerConfiguration;
import play.i18n.Lang;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import plugins.Plugin;
import views.html.error;

/**
 * Created by ilya
 */
public class DcesControllerAction extends Action<DcesController> {

    private static final String[] NOT_EVENT_ACTION = new String[] {
            "/~", "/~res/", "/assets/", "/~global/", "/~dat/", "/bebras_training/" //TODO remove last
    };
    public static final Lang LANG_RU = Lang.forCode("ru");
    public static final Lang LANG_EN = Lang.forCode("en");

    private boolean isEventAction() {
        String path = Http.Context.current().request().path();
        //we have only one slash in the very beginning
        if (path.isEmpty() || path.indexOf('/', 1) < 0)
            return false;

        for (String prefix : NOT_EVENT_ACTION)
            if (path.startsWith(prefix))
                return false;

        return true;
    }

    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        Http.Context.current.set(ctx);

        ServerConfiguration config = ServerConfiguration.getInstance();

        if (config.isMaintenanceMode())
            //TODO попытаться обойтись без времени обслуживания
            return F.Promise.pure(getMaintenanceMessage()); //TODO сделать время возвращения

        //migrate if needed\
        if (config.getDbVersion() != ServerConfiguration.CURRENT_DB_VERSION && !ctx.request().uri().endsWith("/migrate"))
            return F.Promise.pure(getMaintenanceMessage()); //TODO избавиться и от этой хрени тоже

        //initialize plugins

        if (isEventAction()) {
            Event event = Event.current();
            if (event != Event.ERROR_EVENT)
                for (Plugin plugin : event.getPlugins())
                    plugin.initPage();

            // set language for event if it has not already been set
            if (!ctx.args.containsKey("user language was set")) {
                String lang = event.getExtraField("lang", "ru").toString();

                switch (lang) {
                    case "en":
                        ctx.changeLang(LANG_EN);
                        break;
                    default:
                        ctx.changeLang(LANG_RU);
                }
            }
        }

        F.Promise<Result> call = delegate.call(ctx);

        finalizeRequest(ctx);

        boolean forbidCaching = !configuration.allowCache() && !config.isAllowCache();
        if (forbidCaching) {
            // http://stackoverflow.com/questions/49547/making-sure-a-web-page-is-not-cached-across-all-browsers
            ctx.response().setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            ctx.response().setHeader("Pragma", "no-cache");
            ctx.response().setHeader("Expires", "0");
        }

        return call;
    }

    private Result getMaintenanceMessage() {
        return ok(error.render("В данный момент сервер находится в режиме обслуживания, зайдите позже", new String[0]));
    }

    private void finalizeRequest(Http.Context ctx) {
        MongoConnection.storeEnqueuedUsers(ctx);

        ctx.args.put("finalized", true);
    }
}
