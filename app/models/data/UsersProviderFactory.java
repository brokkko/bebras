package models.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 19:06
 */
public class UsersProviderFactory implements ObjectsProviderFactory<User> {

    private static List<String> FIELDS = Arrays.asList("_id", User.FIELD_LOGIN, User.FIELD_EMAIL);
    private static List<String> TITLES = Arrays.asList("id", "login", "email");

    private String role;
    private boolean loadEventResults;
    private String sortField;
    private boolean sortBackwards;
    private boolean onlySubUsers;
    //TODO load results for separate contests

    @Override
    public ObjectsProvider<User> get(Event currentEvent, User currentUser, List<String> searchFields, List<String> searchValues) {
        if (searchFields == null)
            searchFields = Collections.emptyList();

        BasicDBObject query = new BasicDBObject(User.FIELD_EVENT, currentEvent.getId());
        if (role != null)
            query.put(User.FIELD_USER_ROLE, role);

        if (!currentUser.hasEventAdminRight() && onlySubUsers)
            query.put(User.FIELD_REGISTERED_BY, currentUser.getId());

        for (int i = 0; i < searchFields.size(); i++) {
            String field = searchFields.get(i);
            Object value = searchValues.get(i);
            switch (field) {
                case "_id":
                    try {
                        value = new ObjectId((String) value);
                    } catch (IllegalArgumentException ignored) {
                        value = null;
                    }
                    break;
                case "login":
                case "email":
                    break;
                default:
                    continue;
            }
            query.put(field, value);
        }

        DBObject sort = null;
        if (sortField != null)
            sort = new BasicDBObject(sortField, sortBackwards ? -1 : 1);

        return new UsersProvider(loadEventResults, query, sort);
    }

    @Override
    public Class<User> getObjectsClass() {
        return User.class;
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    @Override
    public List<String> getTitles() {
        return TITLES;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("role", role);
        serializer.write("load event results", loadEventResults);
        serializer.write("sort", sortField);
        if (sortField != null)
            serializer.write("sort back", sortBackwards);
        serializer.write("only subusers", onlySubUsers);
    }

    @Override
    public void update(Deserializer deserializer) {
        role = deserializer.readString("role");
        loadEventResults = deserializer.readBoolean("load event results", false);
        sortField = deserializer.readString("sort");
        sortBackwards = deserializer.readBoolean("sort back", false);
        onlySubUsers = deserializer.readBoolean("only subusers", true);
    }
}
