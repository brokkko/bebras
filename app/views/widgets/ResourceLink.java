package views.widgets;

import models.Event;
import models.ServerConfiguration;
import play.Play;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 22:00
 */
public class ResourceLink implements Widget {

    private static final Set<String> SKINNED_CSS = new HashSet<String>() {{
        add("main_with_menu.css");
        add("contest.css");
        add("forms.css");
    }};

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
                if (SKINNED_CSS.contains(localUrl) || SKINNED_CSS.contains(localUrl + ".css")) {
                    String skin = ServerConfiguration.getInstance().getSkin();
                    localUrl = skin + '/' + localUrl;
                }

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

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceLink that = (ResourceLink) o;

        if (externalUrl != null ? !externalUrl.equals(that.externalUrl) : that.externalUrl != null) return false;
        if (localUrl != null ? !localUrl.equals(that.localUrl) : that.localUrl != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = localUrl != null ? localUrl.hashCode() : 0;
        result = 31 * result + (externalUrl != null ? externalUrl.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
