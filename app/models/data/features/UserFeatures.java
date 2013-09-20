package models.data.features;

import models.User;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.forms.RawForm;
import models.newserialization.FlatSerializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 23:59
 */
public class UserFeatures implements FeaturesSet<User> {

    private RawForm rawForm;

    @Override
    public void load(User user) throws Exception {
        FlatSerializer serializer = new FlatSerializer(".");
        user.serialize(serializer);
        rawForm = serializer.getRawForm();
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        if (rawForm == null)
            throw new IllegalStateException("Object not loaded");

        return rawForm.get(featureName);
    }

    @Override
    public void close() throws Exception {
        if (rawForm != null)
            rawForm = null;
    }
}
