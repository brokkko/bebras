package models.data;

import models.Contest;
import models.Event;
import models.newserialization.SerializableUpdatable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 18:12
 */
public interface ObjectsProviderFactory<T> extends SerializableUpdatable {

    ObjectsProvider<T> get();

    Class<T> getObjectsClass();

}
