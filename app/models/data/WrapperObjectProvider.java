package models.data;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.07.13
 * Time: 15:42
 */
public abstract class WrapperObjectProvider<From, To> implements ObjectsProvider<To> {

    private ObjectsProvider<From> delegate;

    protected WrapperObjectProvider(ObjectsProvider<From> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    protected abstract To convert(From object);

    @Override
    public To next() {
        return convert(delegate.next());
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }
}