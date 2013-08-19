package views.widgets;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.08.13
 * Time: 17:16
 */
public class FormsWidget implements Widget {

    private static FormsWidget instance = new FormsWidget();

    private FormsWidget() {
    }

    @Override
    public List<ResourceLink> links() {
        return Arrays.asList(new ResourceLink("forms.css"), new ResourceLink("forms.js"));
    }

    public static FormsWidget get() {
        return instance;
    }
}
