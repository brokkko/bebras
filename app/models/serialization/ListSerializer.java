package models.serialization;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 0:12
 */
public interface ListSerializer {

    void write(Object value);

    Serializer getSerializer();

    ListSerializer getListSerializer();

}
