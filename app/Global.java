import models.problems.ProblemSource;
import models.problems.RootProblemSource;
import models.problems.bbtc.BBTCProblemSource;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.Handler;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 10.01.13
 * Time: 18:26
 */
public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        //TODO move problem sources mount to configuration
        //TODO allow uploading of files to mount
    }

    /*@Override
    public Action onRequest(Http.Request request, Method method) {
        Logger.info("Request: " + request + " -> " + request.remoteAddress() + " " + Arrays.toString(request.headers().get("USER-AGENT")));
        return super.onRequest(request, method);
    }*/

    @Override
    public Result onHandlerNotFound(Http.RequestHeader requestHeader) {
        Logger.info("Handler not found: " + requestHeader.getHeader("USER-AGENT"));
        return super.onHandlerNotFound(requestHeader);
    }

    @Override
    public Result onBadRequest(Http.RequestHeader requestHeader, String error) {
        Logger.info("Bad request: " + requestHeader.getHeader("USER-AGENT") + " -> " + error);
        return super.onBadRequest(requestHeader, error);
    }
}
