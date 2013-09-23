import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 10.01.13
 * Time: 18:26
 */
public class Global extends GlobalSettings {

    //TODO schedule backup http://stackoverflow.com/questions/9339714/where-is-the-job-support-in-play-2-0

    @Override
    public void onStart(Application app) {
    }

    @Override
    public Action onRequest(Http.Request request, Method method) {
        Logger.info("Request: " + request + " -> " + request.remoteAddress() + " " + Arrays.toString(request.headers().get("User-Agent")));
        return super.onRequest(request, method);
    }

    @Override
    public Result onHandlerNotFound(Http.RequestHeader requestHeader) {
        Logger.info("Handler not found: " + requestHeader.getHeader("User-Agent"));
        return super.onHandlerNotFound(requestHeader);
    }

    @Override
    public Result onBadRequest(Http.RequestHeader requestHeader, String error) {
        Logger.info("Bad request: " + requestHeader.getHeader("User-Agent") + " -> " + error);
        return super.onBadRequest(requestHeader, error);
    }
}
