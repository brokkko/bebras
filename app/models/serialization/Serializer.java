package models.serialization;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.03.13
 * Time: 13:56
 */
public interface Serializer {

    void write(String field, Object value);

    Serializer getSerializer(String field);

    ListSerializer getListSerializer(String field);

}
