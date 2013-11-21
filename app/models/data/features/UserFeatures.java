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

    private Map<ObjectId, RawForm> user2form = new HashMap<>();

    @Override
    public void load(User user) throws Exception {
        rawForm = convertUserToForm(user);

        id = user.getId();

        this.user = user;
    }

    private RawForm convertUserToForm(User user) {
        FlatSerializer serializer = new FlatSerializer(".");
        user.serialize(serializer);
        return serializer.getRawForm();
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        if (rawForm == null)
            throw new IllegalStateException("Object not loaded");

        RawForm effectiveRawForm = rawForm;
        User effectiveUser = user;

        while (featureName.startsWith("~reg_by.")) {
            featureName = featureName.substring("~reg_by.".length());

            effectiveUser = effectiveUser.getRegisteredByUser();
            if (effectiveUser == null)
                return null;

            effectiveRawForm = user2form.get(effectiveUser.getId());
            if (effectiveRawForm == null) {
                effectiveRawForm = convertUserToForm(effectiveUser);
                user2form.put(effectiveUser.getId(), effectiveRawForm);
            }
        }

        if (featureName.startsWith("~contest#")) {
            List<Contest> contests = context.getEvent().getContestsAvailableForUser(effectiveUser);
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

        return effectiveRawForm.get(featureName);
    }

    @Override
    public void close() throws Exception {
        user = null;
        rawForm = null;
    }
}
