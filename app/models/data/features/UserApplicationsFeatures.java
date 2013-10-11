package models.data.features;

import models.User;
import models.applications.Application;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 23:59
 */
public class UserApplicationsFeatures implements FeaturesSet<User> {

    private User user;

    @Override
    public void load(User user) throws Exception {
        this.user = user;
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        switch (featureName) {
            case "code":
                return getIds();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        user = null;
    }

    private List<Application> getApplications() {
        //noinspection unchecked
        return (List<Application>) user.getInfo().get("apps"); //TODO extra field is not necessary "apps"
    }

    public Object getIds() {
        Set<String> ids = new HashSet<>();

        for (Application application : getApplications())
            ids.add(application.getCode());
        ids.add(new Application(user, 0, 0, false).getCode());

        String result = "";

        for (String id : ids)
            if (result.isEmpty())
                result = id;
            else
                result += ", " + id;

        return result;
    }
}