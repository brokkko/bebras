package views;

import play.mvc.Call;

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
}
