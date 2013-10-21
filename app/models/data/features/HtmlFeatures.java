package models.data.features;

import models.data.*;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.09.13
 * Time: 1:32
 */
public class HtmlFeatures<T> extends FunctionFeaturesSet<T> {

    public HtmlFeatures(FeaturesSet<T> delegate) {
        super(delegate);
    }

    @Override
    protected Object function(String function, Object feature, FeaturesContext context) {
        if (context.getType() == FeaturesContestType.CSV)
            return feature;

        if (feature == null)
            return feature;

        switch (function) {
            case "bool":
                boolean result;
                if (feature instanceof Boolean)
                    result = (Boolean) feature;
                else
                    result = feature.toString().equals("true");

                return new WrappedFeatureValue(feature, result ? Html.apply("&#x2713;") : Html.apply("&#x2717;"));
        }

        return feature;
    }
}
