package models.data;

import models.User;
import models.applications.ApplicationWithUser;
import models.data.features.*;
import play.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 21:57
 */
public class FeaturesSetRegistry {

    private static final FeaturesSetRegistry instance = new FeaturesSetRegistry();

    public static FeaturesSetRegistry getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> FeaturesSet<T> getFeaturesSet(Class<T> clazz) {
        //TODO optimize. Make FeatureSet immutable and add a class such as a FeatureSetWithLoadedObject
        CompositeFeaturesSet<T> set = new CompositeFeaturesSet<>(".");

        if (clazz.equals(User.class)) {
            set.register("user", (FeaturesSet<T>) new UserFeatures());
            set.register("contest", (FeaturesSet<T>) new ContestHistoryFeatures());
            set.register("user_action", (FeaturesSet<T>) new UserActionsFeatures((FeaturesSet<User>) set));
            set.register("user_apps", (FeaturesSet<T>) new UserApplicationsFeatures());
        }

        if (clazz.equals(ApplicationWithUser.class))
            set.register("app", (FeaturesSet<T>) new ApplicationsFeatures());

        set.register("substring", new SubstringFunctionFeatures(set));
        set.register("const", new ConstFunctionFeatures());
        set.register("excel", new UtilsFeatures(set));
        set.register("html", new HtmlFeatures(set));

        return set;
    }

}