package models.data.features;

import models.Contest;
import models.User;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.forms.RawForm;
import models.newserialization.FlatSerializer;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 23:59
 */
public class UserFeatures implements FeaturesSet<User> {

    private User user;
    private RawForm rawForm;
    private ObjectId id;
    private ObjectId regBy;

    private Map<ObjectId, User> regBy2user = new HashMap<>();

    @Override
    public void load(User user) throws Exception {
        FlatSerializer serializer = new FlatSerializer(".");
        user.serialize(serializer);
        rawForm = serializer.getRawForm();

        id = user.getId();
        regBy = user.getRegisteredBy();

        this.user = user;
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        if (rawForm == null)
            throw new IllegalStateException("Object not loaded");

        if (featureName.startsWith("~contest#")) {
            List<Contest> contests = context.getEvent().getContestsAvailableForUser(user);
            featureName = featureName.substring("~contest#".length());
            int dotPos = featureName.indexOf('.');

            int contestInd;
            String featureTail;
            try {
                if (dotPos < 0) {
                    contestInd = Integer.parseInt(featureName);
                    featureTail = "";
                } else {
                    contestInd = Integer.parseInt(featureName.substring(0, dotPos));
                    featureTail = featureName.substring(dotPos);
                }
            } catch (NumberFormatException e) {
                return null;
            }

            if (contestInd < 1 || contestInd > contests.size())
                return null;

            featureName = "_contests." + contests.get(contestInd - 1).getId() + featureTail;
        }

        if (featureName.startsWith("~")) {
            switch (featureName) {
                case "~reg_by": //TODO this may be much generalised: to take any value of user this was registered by
                    if (regBy == null)
                        return null;
                    User regUser = regBy2user.get(regBy);
                    if (regUser == null) {
                        regUser = User.getInstance("_id", regBy, context.getEvent().getId());
                        if (regUser == null) {
                            regBy = null;
                            return null;
                        }
                        regBy2user.put(regBy, regUser);
                    }

                    return regUser.getInfo().get("org_name");
                case "~oid_inc":
                    return Long.toHexString(id.getInc()).toUpperCase();
                case "~oid_time":
                    return Long.toHexString(id.getTimeSecond()).toUpperCase();
                case "~oid_machine":
                    return Long.toHexString(id.getMachine()).toUpperCase();
                default:
                    return "";
            }
        }

        return rawForm.get(featureName);
    }

    @Override
    public void close() throws Exception {
        user = null;
        rawForm = null;
    }
}
