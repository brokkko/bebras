import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.Handler;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 10.01.13
 * Time: 18:26
 */
public class Global extends GlobalSettings {

    @Override
    public void beforeStart(Application application) {
        Logger.debug("before start");
    }

    @Override
    public void onStart(Application application) {
        Logger.debug("on start");
    }

    @Override
    public void onStop(Application application) {
        Logger.debug("on stop");
    }

    @Override
    public Result onError(Http.RequestHeader requestHeader, Throwable throwable) {
        Logger.debug("on error");
        return super.onError(requestHeader, throwable);
    }

    @Override
    public Action onRequest(Http.Request request, Method method) {
        Logger.debug("on request");
        return super.onRequest(request, method);
    }

    @Override
    public Handler onRouteRequest(Http.RequestHeader requestHeader) {
        Logger.debug("on route request");
        return super.onRouteRequest(requestHeader);
    }

    @Override
    public Result onHandlerNotFound(Http.RequestHeader requestHeader) {
        Logger.debug("on handler not found");
        return super.onHandlerNotFound(requestHeader);
    }

    @Override
    public Result onBadRequest(Http.RequestHeader requestHeader, String s) {
        Logger.debug("on bad request: " + s);
        return super.onBadRequest(requestHeader, s);
    }
}
