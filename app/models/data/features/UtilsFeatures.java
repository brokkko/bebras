package models.data.features;

import models.data.*;
import models.utils.Utils;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 29.08.13
 * Time: 2:35
 */
public class UtilsFeatures<T> extends FunctionFeaturesSet<T> {

    public UtilsFeatures(FeaturesSet<T> delegate) {
        super(delegate);
    }

    @Override
    protected Object function(String function, Object feature, FeaturesContext context) {
        if (feature == null)
            return null;

        switch (function) {
            case "=":
                if (context.getType() != FeaturesContestType.CSV)
                    return feature;
                return new WrappedFeatureValue(feature, "=\"" + feature + "\"");
            case "id_to_date":
                try {
                    Date created = new Date(new ObjectId(feature.toString()).getTime());
                    return Utils.formatObjectCreationTime(created);
                } catch (Exception ignored) { //illegal argument exception, null pointer exception
                    return "";
                }
            default:
                return "unknown util feature";
        }
    }
}
