package models.data.features;

import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.data.FunctionFeaturesSet;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 0:24
 */
public class SubstringFunctionFeatures<T> extends FunctionFeaturesSet<T> {

    public SubstringFunctionFeatures(FeaturesSet<T> delegate) {
        super(delegate);
    }

    @Override
    protected Object function(String function, Object feature, FeaturesContext context) {
        String code = (String) feature;

        if (code == null)
            return null;

        try {
            int pos = function.indexOf('-');
            if (pos < 0)
                throw new IllegalArgumentException("No \"-\" in function name");

            int a = Integer.parseInt(function.substring(0, pos));
            int b = Integer.parseInt(function.substring(pos + 1));

            return code.substring(a, b);
        } catch (Exception e) { //TODO not very good solution to catch all exceptions
            //IllegalArg, NumberFormat, IndexOutOfRange
            return "";
        }
    }
}