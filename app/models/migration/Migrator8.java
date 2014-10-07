package models.migration;

import com.mongodb.DBObject;
import controllers.MongoConnection;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 22.08.13
 * Time: 13:37
 */
public class Migrator8 extends Migrator {

    @Override
    public void migrate() {

        //move users information to
        migrate(MongoConnection.getEventsCollection(), new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject object) {
                String id = (String) object.get("_id");
                if (id.equals("bebras13"))
                    object.put("domain", "bebras.ru");
                else
                    object.put("domain", "on-line.runodog.ru");

                return true;
            }
        });
    }
}
