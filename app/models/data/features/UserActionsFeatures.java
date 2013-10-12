package models.data.features;

import models.User;
import models.data.*;
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
        if (context.getType() != FeaturesContestType.INTERFACE)
            return feature;

        if (feature == null)
            feature = new WrappedFeatureValue(null, "-");

        switch (function) {
            case "view":
                return new WrappedFeatureValue(
                        feature,
                        views.html.htmlfeatures.user_link.render(userId.toString(), context.getEvent().getId(), feature)
                );
            case "remove":
                return new WrappedFeatureValue(
                        feature,
                        views.html.htmlfeatures.action.render(
                                "remove-user-" + userId,
                                "Удалить пользователя",
                                controllers.routes.EventAdministration.removeUser(context.getEvent().getId(), userId.toString()),
                                context.getCurrentCall(),
                                feature
                        )
                );
        }

        return feature;
    }
}
