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
        return Arrays.asList(new ResourceLink("admin.css"), new ResourceLink("admin.js"));
    }

    public static AdminWidget get() {
        return instance;
    }
}
