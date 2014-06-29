package models.migration;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import play.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 22.08.13
 * Time: 13:37
 */
public class Migrator9 extends Migrator {

    @Override
    public void migrate() {

        //move users information to
        migrate(MongoConnection.getUsersCollection(), new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject user) {
                Object role = user.get("_role");
                if (role == null) {
                    Logger.info("no role for user: " + user);
                    return false;
                }

                if (!role.equals("SCHOOL_ORG"))
                    return false;

                BasicDBList apps = (BasicDBList) user.get("apps");
                boolean wasApp = false;
                for (Object oapp : apps) {
                    DBObject app = (DBObject) oapp;
                    Boolean b = (Boolean) app.get("kio");
                    if (b == null) {
                        Logger.info("no kio value specified in application description");
                        b = false;
                    }

                    app.removeField("kio");
                    app.put("type", b ? "bk" : "b");
                    wasApp = true;
                }

                return wasApp;
            }
        });
    }
}
