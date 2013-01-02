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
            "js",
            "jquery-1.8.3.min",
            "ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"
    );

    public static final ResourceLink RESET = new ResourceLink(
            "css",
            "reset",
            null
    );

    private String localUrl;
    private String externalUrl;

    private static String localize(String type, String localUrl) {
        switch (type) {
            case "js":
                return "javascripts/" + localUrl + ".js";
            case "css":
                return "stylesheets/" + localUrl + ".css";
        }
        throw new IllegalArgumentException("Unknown resource link type '" + type + "'");
    }

    public ResourceLink(String type, String localUrl, String externalUrl) {
        this(localize(type, localUrl), externalUrl);
    }

    public ResourceLink(String localUrl) {
        this.localUrl = localUrl;
    }

    public ResourceLink(String localUrl, String externalUrl) {
        this.localUrl = localUrl;
        this.externalUrl = externalUrl;
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
