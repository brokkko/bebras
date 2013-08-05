package models.data;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 21:30
 */
public abstract class ConverterFeaturesSet<From, To> implements FeaturesSet<From> {

    private final FeaturesSet<To> delegate;

    public ConverterFeaturesSet(FeaturesSet<To> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void load(From object) throws Exception {
        delegate.load(convert(object));
    }

    @Override
    public Object getFeature(String featureName) throws Exception {
        return delegate.getFeature(featureName);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    protected abstract To convert(From object);
}
