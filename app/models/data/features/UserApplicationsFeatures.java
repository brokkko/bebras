package models.data.features;

import models.User;
import models.applications.Application;
import models.data.FeaturesContext;
import models.data.FeaturesSet;

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
        if (featureName.startsWith("num_part#"))
            return numberOfPayedParticipants(user, featureName.substring("num_part#".length()));

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

    private static List<Application> getApplications(User user) {
        //noinspection unchecked
        return (List<Application>) user.getInfo().get("apps"); //TODO extra field is not necessary "apps"
    }

    private Object getIds() {
        Set<String> ids = new HashSet<>();

        for (Application application : getApplications(user))
            ids.add(application.getCode());
        ids.add(Application.getCodeForUser(user));

        String result = "";

        for (String id : ids)
            if (result.isEmpty())
                result = id;
            else
                result += ", " + id;

        return result;
    }

    public static int numberOfPayedParticipants(User user, String type) {
        int sum = 0;

        for (Application application : getApplications(user))
            if (application.getState() == Application.CONFIRMED && type.equals(application.getType()))
                sum += application.getSize();

        return sum;
    }
}