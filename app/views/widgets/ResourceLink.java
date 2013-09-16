package views.widgets;

import models.Event;
import play.Play;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 22:00
 */
public class ResourceLink implements Widget {

    public static final ResourceLink JQUERY = new ResourceLink(
            "jquery-1.7.2.min",
//            "jquery-1.10.2.min",
            "js",
//            "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js",
            "//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"
    );

    public static final ResourceLink RESET = new ResourceLink(
            "reset",
            "css",
            null
    );

    private String localUrl;
    private String externalUrl;
    private String type;

    public ResourceLink(String localUrl, String type, String externalUrl) {
        this.localUrl = localize(type, localUrl);
        this.externalUrl = externalUrl;
        this.type = type;
    }

    public ResourceLink(String localUrl) {
        this(localUrl, determineType(localUrl));
    }

    public ResourceLink(String localUrl, String type) {
        this(localUrl, type, null);
    }

    private static String localize(String type, String localUrl) {
        switch (type) {
            case "js":
                localUrl = "javascripts/" + localUrl;
                break;
            case "css":
                String skin = Event.currentId().startsWith("bebras") ? "bebras" : "bbtc";

                localUrl = "stylesheets/" + skin + "/" + localUrl;
                break;
            default:
                throw new IllegalArgumentException("Unknown resource link type '" + type + "'");
        }

        if (localUrl.endsWith("." + type))
            return localUrl;
        else
            return localUrl + "." + type;
    }

    private static String determineType(String url) {
        int point = url.lastIndexOf('.');
        if (point == -1)
            return "";
        return url.substring(point + 1);
    }

    public String apply() {
        boolean returnLocal = false;

        if (externalUrl == null)
            returnLocal = true;
        else {
            Boolean local = Play.application().configuration().getBoolean("resources.local");
            if (local != null && local)
                returnLocal = true;
        }

        if (returnLocal)
            return controllers.routes.Assets.at(localUrl).url();
        else
            return externalUrl;
    }

    @Override
    public String toString() {
        return apply();
    }

    public String getType() {
        return type;
    }

    @Override
    public List<ResourceLink> links() {
        return Arrays.asList(this);
    }
}
