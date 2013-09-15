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
public class ExtraPage extends Plugin {

    private String blockId;
    private boolean global;
    private String right;
    private String title;
    private boolean showInMenu;

    @Override
    public void initPage() {
        if (showInMenu)
            Menu.addMenuItem(title, getCall(), right);
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public Result doGet(String action) {
        if (right != null && !User.currentRole().hasRight(right))
            return Controller.forbidden();

        return Controller.ok(views.html.extra_page.render(global ? "~global" : Event.currentId(), blockId));
    }

    @Override
    public Result doPost(String action) {
        return Controller.notFound();
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("block", blockId);
        serializer.write("global", global);
        serializer.write("right", right);
        serializer.write("title", title);
        if (!showInMenu)
            serializer.write("menu", showInMenu);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        blockId = deserializer.readString("block");
        global = deserializer.readBoolean("global", false);
        right = deserializer.readString("right");
        title = deserializer.readString("title");
        showInMenu = deserializer.readBoolean("menu", true);
    }
}
