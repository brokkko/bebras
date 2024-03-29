package models.applications;

import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.09.13
 * Time: 11:11
 */
public class ApplicationWithUser {

    private Application application;
    private ObjectId userId;
    private String login;
    private String role;

    public ApplicationWithUser(Application application, ObjectId userId, String login, String role) {
        this.application = application;
        this.userId = userId;
        this.login = login;
        this.role = role;
    }

    public Application getApplication() {
        return application;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }
}
