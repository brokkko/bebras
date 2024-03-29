package views.widgets;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.08.13
 * Time: 17:16
 */
public class AdminWidget implements Widget {

    private static AdminWidget instance = new AdminWidget();

    private AdminWidget() {
    }

    @Override
    public List<ResourceLink> links() {
        return Arrays.asList(
//                new ResourceLink("http://cdn.jsdelivr.net/g/ace@1.2.4(min/mode-json.js+min/mode-javascript.js+min/worker-javascript.js+min/worker-json.js+min/theme-monokai.js)"),
                new ResourceLink("ace/ace.js"),
                new ResourceLink("ace/theme-github.js"),
                new ResourceLink("ace/mode-json.js"),
                new ResourceLink("admin.css"),
                new ResourceLink("admin.js"));
    }

    public static AdminWidget get() {
        return instance;
    }
}
