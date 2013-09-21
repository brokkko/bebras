package models.data.features;

import models.User;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.data.FunctionFeaturesSet;
import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.09.13
 * Time: 23:36
 */
public class UserActionsFeatures extends FunctionFeaturesSet<User> {

    private ObjectId userId;

    public UserActionsFeatures(FeaturesSet<User> delegate) {
        super(delegate);
    }

    @Override
    public void load(User object) throws Exception {
        userId = object.getId();
    }

    @Override
    protected Object function(String function, Object feature, FeaturesContext context) {
        if (!context.isScreen())
            return feature;

        if (feature == null)
            feature = "-";

        switch (function) {
            case "view":
                return views.html.htmlfeatures.user_link.render(userId.toString(), context.getEvent().getId(), feature);
        }

        return feature;
    }
}
