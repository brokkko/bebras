package views.htmlblocks;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.User;
import models.newserialization.*;
import org.bson.types.ObjectId;
import play.twirl.api.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 13.08.13
 * Time: 12:01
 */
public class HtmlBlock implements SerializableUpdatable {

    public static HtmlBlock load(String eventId, String name) {
        DBCollection blocksCollection = MongoConnection.getHtmlBlocksCollection();

        BasicDBObject query = new BasicDBObject("name", name);
        query.put("event_id", eventId);

        DBObject blockObject = blocksCollection.findOne(query);

        if (blockObject == null)
            return new HtmlBlock(name, eventId, "");

        HtmlBlock result = new HtmlBlock();
        result.update(new MongoDeserializer(blockObject));

        return result;
    }

    private ObjectId id;
    private String name;
    private String eventId;
    private String html;

    public HtmlBlock() {
    }

    public HtmlBlock(String name, String eventId, String html) {
        this.id = new ObjectId();
        this.name = name;
        this.eventId = eventId;
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public String getName() {
        return name;
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
        serializer.write("name", name);
        serializer.write("html", html);
    }

    @Override
    public void update(Deserializer deserializer) {
        id = deserializer.readObjectId("_id");
        eventId = deserializer.readString("event_id");
        name = deserializer.readString("name");
        html = deserializer.readString("html", "");
    }

    public void store() {
        MongoSerializer serializer = new MongoSerializer();
        serialize(serializer);
        MongoConnection.getHtmlBlocksCollection().save(serializer.getObject());
    }
}
