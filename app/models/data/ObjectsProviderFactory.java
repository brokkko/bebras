package models.data;

import models.Event;
import models.User;
import models.newserialization.SerializableUpdatable;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 18:12
 */
public interface ObjectsProviderFactory<T> extends SerializableUpdatable {

    //must work with searchFields = null, searchValues = null
    ObjectsProvider<T> get(Event currentEvent, User currentUser, List<String> searchFields, List<String> searchValues);

    Class<T> getObjectsClass();

    List<String> getFields();

    List<String> getTitles();

}
