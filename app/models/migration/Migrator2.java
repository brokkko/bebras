package models.migration;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.User;
import models.UserType;
import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 26.07.13
 * Time: 23:49
 */
public class Migrator2 extends Migrator {

    @Override
    public void migrate() {
        // contest collections, change user id as string to user id as ObjectId
        for (DBCollection collection : MongoConnection.getContestCollections()) {
            migrate(collection, new DBObjectTranslator() {
                @Override
                public boolean translate(DBObject object) {
                    Object u = object.get("u");

                    if (u instanceof String) {
                        String stringUserId = (String) u;
                        object.put("u", new ObjectId(stringUserId));
                        return true;
                    }

                    return false;
                }
            });
        }

        // add PARTICIPANT type for all users that are still not participants

        migrate(MongoConnection.getUsersCollection(), new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject object) {
                if (object.get(User.FIELD_USER_TYPE) == null) {
                    object.put(User.FIELD_USER_TYPE, UserType.PARTICIPANT.toString());
                    return true;
                }

                return false;
            }
        });
    }
}
