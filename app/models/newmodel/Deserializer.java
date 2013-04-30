package models.newmodel;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.03.13
 * Time: 14:06
 */
public interface Deserializer {

    int getInt(String field);

    Boolean getBoolean(String field);

    String getString(String field);

    Object getObject(String field);

    Deserializer getDeserializer(String field);

    ListDeserializer getListDeserializer(String field);

    Set<String> fieldSet();
}
