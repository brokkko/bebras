package plugins;

import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.mvc.Controller;
import play.mvc.Result;
import views.Menu;
import views.html.extra_page;
import views.html.extra_page_2col;

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
    private boolean twoColumns;

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
    public Result doGet(String action, String params) {
        if (right != null && !User.currentRole().hasRight(right) && !right.equals("anon")) //TODO remove anon role
            return Controller.forbidden();

        if (twoColumns)
            return Controller.ok(extra_page_2col.render(global ? "~global" : Event.currentId(), blockId));
        else
            return Controller.ok(extra_page.render(global ? "~global" : Event.currentId(), blockId));
    }

    @Override
    public Result doPost(String action, String params) {
        return Controller.notFound();
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        if (!getDefaultBlockId().equals(blockId))
            serializer.write("block", blockId);
        if (global)
            serializer.write("global", true);
        serializer.write("right", right);
        serializer.write("title", title);
        if (!showInMenu)
            serializer.write("menu", false);
        if (twoColumns)
            serializer.write("two columns", true);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        blockId = deserializer.readString("block", getDefaultBlockId());
        global = deserializer.readBoolean("global", false);
        right = deserializer.readString("right");
        title = deserializer.readString("title");
        showInMenu = deserializer.readBoolean("menu", true);
        twoColumns = deserializer.readBoolean("two columns", false);
    }

    private String getDefaultBlockId() {
        return "extra_page_" + getRef();
    }
}
