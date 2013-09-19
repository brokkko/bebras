package models.data;

import com.mongodb.BasicDBObject;
import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.mvc.Call;
import play.mvc.Controller;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 19:06
 */
public class UsersProviderFactory implements ObjectsProviderFactory<User> {

    private String role;
    private boolean loadEventResults;
    //TODO load results for separate contests

    @Override
    public ObjectsProvider<User> get(Event currentEvent, User currentUser) {
        BasicDBObject query = new BasicDBObject(User.FIELD_EVENT, currentEvent.getId());
        if (role != null)
            query.put(User.FIELD_USER_ROLE, role);

        if (!currentUser.hasEventAdminRight())
            query.put(User.FIELD_REGISTERED_BY, currentUser.getId());

        return new UsersProvider(loadEventResults, query, null);
    }

    @Override
    public Class<User> getObjectsClass() {
        return User.class;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("role", role);
        serializer.write("load event results", loadEventResults);
    }

    @Override
    public void update(Deserializer deserializer) {
        role = deserializer.readString("role");
        loadEventResults = deserializer.readBoolean("load event results", false);
    }
}
