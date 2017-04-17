package plugins;

import models.Event;
import models.User;
import models.newserialization.*;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import views.Menu;
import views.html.extra_page;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 29.08.13
 * Time: 19:34
 */
public class ExtraPage extends Plugin {

    private final ListSerializationType<SubPage> PAGES_LIST_TYPE = SerializationTypesRegistry.list(
            new SerializableSerializationType<>(SubPage.class)
    );
    
    private String right; //право на просмотр
    private String title; //текст на кнопке меню
    private boolean showInMenu; //показывать ли в меню

    private List<SubPage> subpages = new ArrayList<>();

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
    public F.Promise<Result> doGet(String action, String pageId) {
        if (right != null && !User.currentRole().hasRight(right) && !right.equals("anon")) //TODO remove anon role
            return F.Promise.pure(Controller.forbidden());

        SubPage pageToShow = subpages.get(0);
        for (SubPage subpage : subpages)
            if (pageId.equals(subpage.pageId)) {
                pageToShow = subpage;
                break;
            }

        return F.Promise.pure(Controller.ok(extra_page.render(pageToShow.isGlobal() ? "~global" : Event.currentId(), pageToShow, subpages)));
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);

        serializer.write("right", right);
        serializer.write("title", title);
        if (!showInMenu)
            serializer.write("menu", false);

        PAGES_LIST_TYPE.write(serializer, "subpages", subpages);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        right = deserializer.readString("right");
        title = deserializer.readString("title");
        showInMenu = deserializer.readBoolean("menu", true);

        //first try to read pages
        subpages = PAGES_LIST_TYPE.read(deserializer, "subpages");
        for (SubPage subpage : subpages)
            subpage.setPlugin(this);

        //if nothing was read, read again
        if (subpages.isEmpty()) {
            String blockId = deserializer.readString("block", getDefaultBlockId());
            boolean global = deserializer.readBoolean("global", false);
            boolean twoColumns = deserializer.readBoolean("two columns", false);

            subpages.add(new SubPage(this, null, "", blockId, global, twoColumns));
        }
    }

    private String getDefaultBlockId() {
        return "extra_page_" + getRef();
    }

    public static class SubPage implements SerializableUpdatable {
        private ExtraPage plugin;

        private String pageId; //id подстраницы для ссылки, null если единственный пункт
        private String subtitle; //названия на дополнительных пунктах меню
        private String blockId; //название html блока для хранения страницы
        private boolean global; //является ли html блок глобальным, т.е. одинаковым для всех событий
        private boolean twoColumns; //отображать ли в двух колонках

        public SubPage() {}

        public SubPage(ExtraPage plugin, String pageId, String subtitle, String blockId, boolean global, boolean twoColumns) {
            this.plugin = plugin;
            this.pageId = pageId;
            this.subtitle = subtitle;
            this.blockId = blockId;
            this.global = global;
            this.twoColumns = twoColumns;
        }

        private void setPlugin(ExtraPage plugin) {
            this.plugin = plugin;
        }

        public String getPageId() {
            return pageId;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getBlockId() {
            return blockId == null ? getDefaultBlockId() : blockId;
        }

        public boolean isGlobal() {
            return global;
        }

        public boolean isTwoColumns() {
            return twoColumns;
        }

        public Call getCall() {
            return plugin.getCall("go", true, getPageId() == null ? "" : getPageId());
        }

        @Override
        public void serialize(Serializer serializer) {
            if (pageId != null)
                serializer.write("id", pageId);
            if (subtitle != null && !subtitle.isEmpty())
                serializer.write("title", subtitle);
            
            if (blockId != null)
                serializer.write("block", blockId);
            if (global)
                serializer.write("global", true);
            if (twoColumns)
                serializer.write("two columns", true);
        }

        @Override
        public void update(Deserializer deserializer) {
            pageId = deserializer.readString("id");
            subtitle = deserializer.readString("title", "");
            blockId = deserializer.readString("block");
            global = deserializer.readBoolean("global", false);
            twoColumns = deserializer.readBoolean("two columns", false);
        }

        private String getDefaultBlockId() {
            return "extra_page_" + plugin.getRef() + (pageId != null ? "__" + pageId : "");
        }
    }

}
