package models.data;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 0:13
 */
public abstract class FunctionFeaturesSet<T> implements FeaturesSet<T> {

    private static final char DELIMITER = '|';

    private FeaturesSet<T> delegate;

    public FunctionFeaturesSet(FeaturesSet<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void load(T object) throws Exception {
        //do nothing
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) throws Exception {
        int pos = featureName.indexOf(DELIMITER);
        if (pos < 0)
            throw new IllegalArgumentException("feature without function name");
        String function = featureName.substring(0, pos);
        String value = featureName.substring(pos + 1);

        Object feature = delegate.getFeature(value, context);

        return function(function, feature, context);
    }

    @Override
    public void close() throws Exception {
        //do nothing
    }

    protected abstract Object function(String function, Object feature, FeaturesContext context);
}
