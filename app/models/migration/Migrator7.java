package models.migration;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import controllers.MongoConnection;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 22.08.13
 * Time: 13:37
 */
public class Migrator7 extends Migrator {

    @Override
    public void migrate() {

        //move users information to
        migrate(MongoConnection.getEventsCollection(), new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject object) {
                Object users = object.get("users");
                object.removeField("users");
                BasicDBList roles = (BasicDBList) object.get("roles");
                for (Object role : roles) {
                    ((DBObject) role).put("info", users);
                }

                return true;
            }
        });
    }
}
