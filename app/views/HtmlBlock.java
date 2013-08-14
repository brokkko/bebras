package views;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 13.08.13
 * Time: 12:01
 */
public class HtmlBlock implements SerializableUpdatable {

    private String html;

    public Html format() {
//        return html_block.render();
        return null;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("html", html);
    }

    @Override
    public void update(Deserializer deserializer) {
        html = deserializer.readString("html");
    }
}
