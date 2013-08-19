package views.widgets;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.08.13
 * Time: 18:39
 */
public class CssJsWidget implements Widget {

    private final List<ResourceLink> links;

    private CssJsWidget(String root) {
        ResourceLink css = new ResourceLink(root, "css");
        ResourceLink js = new ResourceLink(root, "js");
        links = Arrays.asList(css, js);
    }

    @Override
    public List<ResourceLink> links() {
        return links;
    }

    public static CssJsWidget get(String root) {
        return new CssJsWidget(root);
    }
}
