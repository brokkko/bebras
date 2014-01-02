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
    private String login;

    public UserActionsFeatures(FeaturesSet<User> delegate) {
        super(delegate);
    }

    @Override
    public void load(User object) throws Exception {
        userId = object.getId();
        login = object.getLogin();
    }

    @Override
    protected Object function(String function, Object feature, FeaturesContext context) {
        if (context.getType() != FeaturesContestType.INTERFACE)
            return feature;

        if (feature == null)
            feature = new WrappedFeatureValue(null, "-");

        if (function.startsWith("swap-flag-")) {
            String flag = function.substring("swap-flag-".length());
            return new WrappedFeatureValue(
                    feature,
                    views.html.htmlfeatures.action.render(
                            "swap-flag-for-" + userId,
                            "Изменить флаг " + flag + " для пользователя",
                            controllers.routes.UserInfo.swapFlag(context.getEvent().getId(), userId.toString(), flag),
                            context.getCurrentCall(),
                            String.valueOf(feature)
                    )
            );
        }

        switch (function) {
            case "view":
                return new WrappedFeatureValue(
                        feature,
                        views.html.htmlfeatures.user_link.render(userId.toString(), context.getEvent().getId(), feature)
                );
            case "certificate": //TODO move this feature to plugin
                return new WrappedFeatureValue(
                        feature,
                        views.html.htmlfeatures.certificate_link.render(login, "eval_places", context.getEvent().getId(), feature)
                );
            case "remove":
                return new WrappedFeatureValue(
                        feature,
                        views.html.htmlfeatures.action.render(
                                "remove-user-" + userId,
                                "Удалить пользователя",
                                controllers.routes.UserInfo.removeUser(context.getEvent().getId(), userId.toString()),
                                context.getCurrentCall(),
                                feature
                        )
                );
        }

        return feature;
    }
}
