package models.migration;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import controllers.MongoConnection;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.08.13
 * Time: 0:05
 */
public class Migrator6 extends Migrator {
    @Override
    public void migrate() {
        //move type to role
        migrate(MongoConnection.getUsersCollection(), new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject object) {
                String type = (String) object.get("_type");
                object.removeField("_type");
                object.put("_role", type);
                return true;
            }
        });

        //add roles to all events
        migrate(MongoConnection.getEventsCollection(), new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject object) {
                DBObject participantRole = new BasicDBObject("name", "PARTICIPANT");
                DBObject adminRole = new BasicDBObject("name", "EVENT_ADMIN");

                BasicDBList adminRights = new BasicDBList();
                adminRights.add("event admin");
                adminRole.put("rights", adminRights);

                BasicDBList rolesList = new BasicDBList();
                rolesList.add(participantRole);
                rolesList.add(adminRole);

                object.put("roles", rolesList);

                return true;
            }
        });
    }
}
