package models.data.features;

import models.User;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.forms.RawForm;
import models.newserialization.FlatSerializer;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 23:59
 */
public class UserFeatures implements FeaturesSet<User> {

    private RawForm rawForm;
    private ObjectId regBy;

    private Map<ObjectId, User> regBy2user = new HashMap<>();

    @Override
    public void load(User user) throws Exception {
        FlatSerializer serializer = new FlatSerializer(".");
        user.serialize(serializer);
        rawForm = serializer.getRawForm();

        regBy = user.getRegisteredBy();
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        if (rawForm == null)
            throw new IllegalStateException("Object not loaded");

        if (featureName.equals("~reg_by")) { //this may be much generalized
            if (regBy == null)
                return null;
            User regUser = regBy2user.get(regBy);
            if (regUser == null) {
                regUser = User.getInstance("_id", regBy, context.getEvent().getId());
                regBy2user.put(regBy, regUser);
            }

            return regUser.getInfo().get("org_name");
        }

        return rawForm.get(featureName);
    }

    @Override
    public void close() throws Exception {
        if (rawForm != null)
            rawForm = null;
    }
}
