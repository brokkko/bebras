package models.applications;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.Event;
import models.User;
import models.applications.Application;
import models.applications.ApplicationWithUser;
import models.data.ObjectsProvider;
import models.newserialization.ListDeserializer;
import models.newserialization.MongoListDeserializer;
import models.newserialization.SerializableSerializationType;
import models.newserialization.SerializationType;
import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.09.13
 * Time: 2:32
 */
public class ApplicationsProvider implements ObjectsProvider<ApplicationWithUser> {

    private static SerializationType<Application> APPLICATION_SERIALIZATION_TYPE = new SerializableSerializationType<>(Application.class);

    private DBCursor cursor;
    private ListDeserializer currentUserApplications = null;
    private int state;
    private String name;

    private ApplicationWithUser nextApplication;
    private String nextLogin;
    private ObjectId nextUserId;

    //negative state means all states
    public ApplicationsProvider(Event currentEvent, User currentUser, String role, int state, String name, String login) {
        this.state = state;
        this.name = name;

        BasicDBObject query = new BasicDBObject(User.FIELD_EVENT, currentEvent.getId());
        query.put(User.FIELD_USER_ROLE, role);

        if (state < 0)
            query.put("apps", new BasicDBObject("$exists", true));
        else
            query.put("apps.state", state);

        if (name != null)
            query.put("apps.name", name);

        if (login != null)
            query.put("login", login);

        if (!currentUser.hasEventAdminRight())
            query.put(User.FIELD_REGISTERED_BY, currentUser.getId());

        BasicDBObject projection = new BasicDBObject("apps", 1);
        projection.put("_id", 1); // not sure this needed, _id is projected by itself
        projection.put("login", 1);

        cursor = MongoConnection.getUsersCollection().find(query, projection); //projection select only apps

        searchNextApplicationsList();
        searchNextApplication();
    }

    @Override
    public boolean hasNext() {
        return nextApplication != null;
    }

    @Override
    public ApplicationWithUser next() {
        ApplicationWithUser result = nextApplication;
        searchNextApplication();
        return result;
    }

    @Override
    public void close() throws Exception {
        if (cursor != null)
            cursor.close();
    }

    private void searchNextApplication() {
        while (true) {
            if (currentUserApplications == null) {
                nextApplication = null;
                return;
            }

            if (!currentUserApplications.hasMore()) {
                nextApplication = null;
                searchNextApplicationsList();
                continue;
            }

            Application nextApplication = APPLICATION_SERIALIZATION_TYPE.read(currentUserApplications);

            if (state >= 0 && nextApplication.getState() != state)
                continue;

            if (name != null && !name.equals(nextApplication.getName()))
                continue;

            this.nextApplication = new ApplicationWithUser(nextApplication, nextUserId, nextLogin);
            break;
        }
    }

    private void searchNextApplicationsList() {
        if (!cursor.hasNext()) {
            currentUserApplications = null;
            return;
        }

        DBObject nextObject = cursor.next();
        BasicDBList apps = (BasicDBList) nextObject.get("apps");

        currentUserApplications = new MongoListDeserializer(apps);
        nextUserId = (ObjectId) nextObject.get("_id");
        nextLogin = (String) nextObject.get("login");
    }

}
