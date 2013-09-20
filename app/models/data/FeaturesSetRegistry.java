package models.data;

import models.User;
import models.data.features.*;

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
        CompositeFeaturesSet<T> set = new CompositeFeaturesSet<>(".");

        if (clazz.equals(User.class)) {
            set.register("user", (FeaturesSet<T>) new UserFeatures());
            set.register("contest", (FeaturesSet<T>) new ContestHistoryFeatures());
        }

        set.register("substring", new SubstringFunctionFeatures(set));
        set.register("const", new ConstFunctionFeatures());
        set.register("excel", new ExcelFeatures(set));

        return set;
    }

}