package models.data;

import models.results.Info;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 16:22
 */
public class InfoFeatureSet implements FeaturesSet<Info> {

    private Info object;

    @Override
    public void load(Info object) throws Exception {
        this.object = object;
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        return object.get(featureName);
    }

    @Override
    public void close() throws Exception {
    }
}
