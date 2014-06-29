package models.newserialization;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.08.13
 * Time: 22:26
 */
public class EmptyListSerializer extends ListSerializer {
    @Override
    public void write(int value) {}

    @Override
    public void write(long value) {}

    @Override
    public void write(double value) {}

    @Override
    public void write(boolean value) {}

    @Override
    public void write(String value) {}

    @Override
    public Serializer getSerializer() {
        return new EmptySerializer();
    }

    @Override
    public ListSerializer getListSerializer() {
        return new EmptyListSerializer();
    }
}
