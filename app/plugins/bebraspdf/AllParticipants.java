package plugins.bebraspdf;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import controllers.MongoConnection;
import models.User;
import models.newserialization.MongoDeserializer;
import models.results.Info;
import org.bson.types.ObjectId;
import plugins.bebraspdf.model.UserResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 21.10.13
 * Time: 16:16
 */
public class AllParticipants {

    private static final String SEPARATOR = "|||";

    private Map<String, User> data2user = new HashMap<>();

    public AllParticipants(String participantRole, String participantField, ObjectId organizer) {
        BasicDBObject query = new BasicDBObject(User.FIELD_REGISTERED_BY, organizer);
        query.put(participantField, true);
        query.put(User.FIELD_USER_ROLE, participantRole);

        try (DBCursor cursor = MongoConnection.getUsersCollection().find(query)) {
            while (cursor.hasNext()) {
                User user = User.deserialize(new MongoDeserializer(cursor.next()));

                String userKey = constructUserKey(user);
                data2user.put(userKey, user);
            }
        }
    }

    private String constructUserKey(User user) {
        Info info = user.getInfo();

        String name = String.valueOf(info.get("name")).toLowerCase();
        String surname = String.valueOf(info.get("surname")).toLowerCase();
        String grade = String.valueOf(info.get("grade")).toLowerCase();

        return name + SEPARATOR + surname + SEPARATOR + grade;
    }

    public User getUserByHisOrHerResults(UserResult result) {
        String name = String.valueOf(result.getPdfUser().getName()).toLowerCase();
        String surname = String.valueOf(result.getPdfUser().getSurname()).toLowerCase();
        String grade = String.valueOf(result.getUserClass().getClassNumber());

        String key = name + SEPARATOR + surname + SEPARATOR + grade;
        return data2user.get(key);
    }

    public void addUser(User user) {
        String userKey = constructUserKey(user);
        data2user.put(userKey, user);
    }
}
