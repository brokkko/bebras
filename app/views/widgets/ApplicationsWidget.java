package views.widgets;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.08.13
 * Time: 17:16
 */
public class ApplicationsWidget implements Widget {

    private static ApplicationsWidget instance = new ApplicationsWidget();

    private ApplicationsWidget() {
    }

    @Override
    public List<ResourceLink> links() {
        return Arrays.asList(new ResourceLink("applications.css"), new ResourceLink("applications.js"));
    }

    public static ApplicationsWidget get() {
        return instance;
    }
}
