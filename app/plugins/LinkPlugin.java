package plugins;

import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.mvc.Controller;
import play.mvc.Result;
import views.Menu;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 29.08.13
 * Time: 19:34
 */
public class LinkPlugin extends Plugin {

    private String right;
    private String title;
    private String link;

    @Override
    public void initPage() {
        Menu.addMenuItem(title, getCall(), right);
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public Result doGet(String action, String params) {
        if (right != null && !User.currentRole().hasRight(right) && !right.equals("anon")) //TODO remove anon role
            return Controller.forbidden();

        return Controller.redirect(link);
    }

    @Override
    public Result doPost(String action, String params) {
        return Controller.notFound();
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("right", right);
        serializer.write("title", title);
        serializer.write("link", link);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        right = deserializer.readString("right");
        title = deserializer.readString("title");
        link = deserializer.readString("link");
    }

}
