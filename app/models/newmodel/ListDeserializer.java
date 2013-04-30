package models.newmodel;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 12:27
 */
public interface ListDeserializer {

    boolean hasMore();

    int getInt();

    boolean getBoolean();

    String getString();

    Object getObject();

    Deserializer getDeserializer();

    ListDeserializer getListDeserializer();

}
