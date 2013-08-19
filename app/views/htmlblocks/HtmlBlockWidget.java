package views.htmlblocks;

import models.User;
import models.UserType;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.08.13
 * Time: 21:09
 */
public class HtmlBlockWidget implements Widget {

    private static final ResourceLink css = new ResourceLink("html-block.css");
    private static final ResourceLink jsEditor = new ResourceLink("wymeditor/jquery.wymeditor.min.js");
    private static final ResourceLink jsBlock = new ResourceLink("html-block.js");

    private final List<ResourceLink> links;

    private HtmlBlockWidget() {
        if (User.current().getType() == UserType.EVENT_ADMIN)
            links = Arrays.asList(css, jsEditor, jsBlock);
        else
            links = Arrays.asList(css);
    }

    @Override
    public List<ResourceLink> links() {
        return links;
    }

    public static HtmlBlockWidget get() {
        return new HtmlBlockWidget();
    }

}
