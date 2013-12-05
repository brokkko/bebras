import models.Announcement;
import models.ServerConfiguration;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 10.01.13
 * Time: 18:26
 */
public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        Logger.info("Application started");
        Announcement.scheduleOneSending(1);
    }

    @Override
    public Action onRequest(Http.Request request, Method method) {
        String ip = request.remoteAddress();
        if (ServerConfiguration.getInstance().isIpTraced(ip)) {
            StringBuilder info = new StringBuilder();

            info.append("Request from traced ip ").append(request.remoteAddress()).append("\n");
            info.append(new Date().toString()).append("\n");
            info.append(request.method()).append(" ").append(request.host()).append(" ").append(request.uri());
            info.append("\n");
            Map<String,String[]> headers = request.headers();
            for (Map.Entry<String, String[]> headerEntry : headers.entrySet()) {
                String header = headerEntry.getKey();
                for (String value : headerEntry.getValue())
                    info.append(header).append(": ").append(value).append("\n");
            }

//            info.append("*Cookies*\n");

            Logger.info(info.toString());
        }
        return super.onRequest(request, method);
    }

    /*@Override
    public Result onHandlerNotFound(Http.RequestHeader requestHeader) {
        Logger.info("Handler not found: " + requestHeader.method() + " " + requestHeader.host() + requestHeader.uri());
        return super.onHandlerNotFound(requestHeader);
    }*/

    @Override
    public Result onBadRequest(Http.RequestHeader requestHeader, String error) {
        Logger.info("Bad request: " + requestHeader.method() + " " + requestHeader.host() + requestHeader.uri());
        return super.onBadRequest(requestHeader, error);
    }
}
