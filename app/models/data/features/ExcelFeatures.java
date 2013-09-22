package models.data.features;

import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.data.FunctionFeaturesSet;
import models.data.WrappedFeatureValue;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 29.08.13
 * Time: 2:35
 */
public class ExcelFeatures<T> extends FunctionFeaturesSet<T> {

    public ExcelFeatures(FeaturesSet<T> delegate) {
        super(delegate);
    }

    @Override
    protected Object function(String function, Object feature, FeaturesContext context) {
        if (context.isScreen())
            return feature;

        if (feature == null)
            return null;

        switch (function) {
            case "=":
                return new WrappedFeatureValue(feature, "=\"" + feature + "\"");
            default:
                return "unknown excel feature";
        }
    }
}
