package views.widgets;

import models.Event;
import play.Play;
import play.mvc.Call;

import java.util.*;

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
//            "jquery-1.11.3.min",
            "jquery-1.7.2.min",
            "js",
            "//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"
//            "//ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"
    );

    public static final ResourceLink RESET = new ResourceLink(
            "reset",
            "css",
            null
    );

    private static final String resourcesVersion;

    static {
        resourcesVersion = Play.application().configuration().getString("resources_version");
    }

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
        if (localUrl.startsWith("data:"))
            return localUrl;

        switch (type) {
            case "js":
                localUrl = "javascripts/" + localUrl;
                break;
            case "css":
                if (SKINNED_CSS.contains(localUrl) || SKINNED_CSS.contains(localUrl + ".css")) {
                    String skin = Event.current().getSkin();
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

    public static Call getFavicon() {
        String localUrl = "images/favicon." + Event.current().getSkin() + ".png";
        return controllers.routes.Assets.at(versionizeUrl(localUrl));
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

        if (returnLocal) {
            return controllers.routes.Assets.at(versionizeUrl(localUrl)).url();
        } else
            return externalUrl;
    }

    private static String versionizeUrl(String url) {
        return resourcesVersion == null ? url : url + "?" + resourcesVersion;
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

        if (!Objects.equals(externalUrl, that.externalUrl)) return false;
        if (!Objects.equals(localUrl, that.localUrl)) return false;
        if (!Objects.equals(type, that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = localUrl != null ? localUrl.hashCode() : 0;
        result = 31 * result + (externalUrl != null ? externalUrl.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public String getLocalUrl() {
        return localUrl;
    }
}
