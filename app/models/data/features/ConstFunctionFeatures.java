package models.data.features;

import models.data.FeaturesContext;
import models.data.FeaturesSet;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 21:58
 */
public class ConstFunctionFeatures<T> implements FeaturesSet<T> {

    @Override
    public void load(T object) throws Exception {
        //do nothing
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        return featureName;
    }

    @Override
    public void close() throws Exception {
        //do nothing
    }
}
