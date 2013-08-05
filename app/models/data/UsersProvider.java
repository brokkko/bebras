package models.data;

import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.User;
import models.newserialization.MongoDeserializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 18:13
 */
public class UsersProvider extends WrapperObjectProvider<DBObject, User> {

    private boolean loadEventResults;

    protected UsersProvider(boolean loadEventResults, DBObject query, DBObject sort) {
        super(new MongoQueryObjectsProvider(
                MongoConnection.getUsersCollection(),
                query,
                sort
        ));

        this.loadEventResults = loadEventResults;
    }

    @Override
    protected User convert(DBObject object) {
        MongoDeserializer deserializer = new MongoDeserializer(object);
        User user = new User();
        user.update(deserializer);

        if (loadEventResults)
            user.getEventResults();

        return user;
    }

}