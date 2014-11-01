import models.Announcement;
import models.ServerConfiguration;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.PlayException;
import play.mvc.SimpleResult;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;
import java.util.Arrays;
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

    @Override
    public F.Promise<SimpleResult> onBadRequest(Http.RequestHeader requestHeader, String error) {
        Logger.info("Bad request: " + requestHeader.method() + " " + requestHeader.host() + requestHeader.uri());
        return super.onBadRequest(requestHeader, error);
    }

    @Override
    public F.Promise<SimpleResult> onError(Http.RequestHeader request, Throwable t) {
        if (t instanceof PlayException) {
            String exceptionId = ((PlayException) t).id;

            StringBuilder headers = new StringBuilder();
            headers.append(request.method()).append(" ").append(request.host()).append(request.uri()).append("\n");
            for (Map.Entry<String, String[]> entry : request.headers().entrySet())
                headers.append(entry.getKey()).append(": ").append(Arrays.toString(entry.getValue())).append("\n");

            Logger.error("Headers for exception @" + exceptionId + "\n" + headers);
        }
        return super.onError(request, t);
    }
}
