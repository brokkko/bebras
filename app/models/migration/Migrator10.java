package models.migration;

import com.mongodb.DBObject;
import controllers.MongoConnection;
import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 22.08.13
 * Time: 13:37
 */
public class Migrator10 extends Migrator {

    @Override
    public void migrate() {

        //move users information to
        migrate(MongoConnection.getHtmlBlocksCollection(), new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject block) {
                String name = (String) block.get("_id");
                block.put("_id", new ObjectId());
                block.put("name", name);
                return true;
            }
        });
    }
}
