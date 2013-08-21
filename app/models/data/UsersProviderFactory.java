package models.data;

import com.mongodb.BasicDBObject;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 19:06
 */
public class UsersProviderFactory implements ObjectsProviderFactory<User> {

    private String eventId;
    private String role;
    private boolean loadEventResults;
    //TODO load results for separate contests

    @Override
    public ObjectsProvider<User> get() {
        BasicDBObject query = new BasicDBObject(User.FIELD_EVENT, eventId);
        if (role != null)
            query.put(User.FIELD_USER_ROLE, role);

        return new UsersProvider(loadEventResults, query, null);
    }

    @Override
    public Class<User> getObjectsClass() {
        return User.class;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("role", role);
        serializer.write("event", eventId);
        serializer.write("load event results", loadEventResults);
    }

    @Override
    public void update(Deserializer deserializer) {
        role = deserializer.readString("role");
        eventId = deserializer.readString("event");
        loadEventResults = deserializer.readBoolean("load event results", false);
    }
}
