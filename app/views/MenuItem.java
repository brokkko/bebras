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

    private final String title;
    private final Call link;
    private final String target;
    private final int priority;

    public MenuItem(String title, Call link) {
        this.title = title;
        this.link = link;
        this.target = null;
        this.priority = 0;
    }

    public MenuItem(String title, Call link, String target, int priority) {
        this.title = title;
        this.link = link;
        this.target = target;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public Call getLink() {
        return link;
    }

    public String getTarget() {
        return target;
    }

    public boolean isCurrent() {
        return link != null && Http.Context.current().request().path().equals(link.url()); //TODO make sure this works in all situations;
    }

    public int getPriority() {
        return priority;
    }
}
