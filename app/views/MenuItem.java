package views;

import play.mvc.Call;
import play.mvc.Http;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 08.01.13
 * Time: 23:46
 */
public class MenuItem {

    private String title;
    private Call link;

    public MenuItem(String title, Call link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public Call getLink() {
        return link;
    }

    public boolean isCurrent() {
        return link != null && Http.Context.current().request().path().equals(link.url()); //TODO make sure this works in all situations;
    }
}
