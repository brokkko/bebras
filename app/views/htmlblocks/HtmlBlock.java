package views.htmlblocks;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.Event;
import models.User;
import models.newserialization.*;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 13.08.13
 * Time: 12:01
 */
public class HtmlBlock implements SerializableUpdatable {

    public static HtmlBlock load(String eventId, String id) {
        DBCollection blocksCollection = MongoConnection.getHtmlBlocksCollection();

        BasicDBObject query = new BasicDBObject("_id", id);
        query.put("event_id", eventId);

        DBObject blockObject = blocksCollection.findOne(query);

        if (blockObject == null)
            return new HtmlBlock(id, eventId, "");

        HtmlBlock result = new HtmlBlock();
        result.update(new MongoDeserializer(blockObject));

        return result;
    }

    private String id;
    private String eventId;
    private String html;

    public HtmlBlock() {
    }

    public HtmlBlock(String id, String eventId, String html) {
        this.id = id;
        this.eventId = eventId;
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setHtml(String html) {
        this.html = html;
        store();
    }

    public Html format() {
        boolean mayEdit = User.currentRole().hasRight("event admin");
        return views.html.htmlblocks.html_block.render(this, mayEdit);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("_id", id);
        serializer.write("event_id", eventId);
        serializer.write("html", html);
    }

    @Override
    public void update(Deserializer deserializer) {
        id = deserializer.readString("_id");
        eventId = deserializer.readString("event_id");
        html = deserializer.readString("html", "");
    }

    public void store() {
        MongoSerializer serializer = new MongoSerializer();
        serialize(serializer);
        MongoConnection.getHtmlBlocksCollection().save(serializer.getObject());
    }
}
