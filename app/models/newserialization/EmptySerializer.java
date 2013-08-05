package models.newserialization;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.08.13
 * Time: 22:23
 */
public class EmptySerializer extends Serializer {

    @Override
    public void write(String field, int value) {}

    @Override
    public void write(String field, long value) {}

    @Override
    public void write(String field, double value) {}

    @Override
    public void write(String field, boolean value) {}

    @Override
    public void write(String field, String value) {}

    @Override
    public Serializer getSerializer(String field) {
        return new EmptySerializer();
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        return new EmptyListSerializer();
    }
}
