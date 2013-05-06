package views;

import controllers.routes;
import play.Play;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 22:00
 */
public class ResourceLink {

    public static final ResourceLink JQUERY = new ResourceLink(
            "jquery-1.7.2.min",
            "js",
            "//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"
    );

    public static final ResourceLink RESET = new ResourceLink(
            "reset",
            "css",
            null
    );

    private String localUrl;
    private String externalUrl;

    private static String localize(String type, String localUrl) {
        switch (type) {
            case "js":
                localUrl = "javascripts/" + localUrl;
                break;
            case "css":
                localUrl = "stylesheets/" + localUrl;
                break;
            default:
                throw new IllegalArgumentException("Unknown resource link type '" + type + "'");
        }

        if (localUrl.endsWith("." + type))
            return localUrl;
        else
            return localUrl + "." + type;
    }

    public ResourceLink(String localUrl, String type, String externalUrl) {
        this.localUrl = localize(type, localUrl);
        this.externalUrl = externalUrl;
    }

    public ResourceLink(String localUrl) {
        this(localUrl, determineType(localUrl));
    }

    private static String determineType(String url) {
        int point = url.lastIndexOf('.');
        if (point == -1)
            return "";
        return url.substring(point + 1);
    }

    public ResourceLink(String localUrl, String type) {
        this(localUrl, type, null);
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
            return routes.Assets.at(localUrl).url();
        else
            return externalUrl;
    }

    @Override
    public String toString() {
        return apply();
    }
}
