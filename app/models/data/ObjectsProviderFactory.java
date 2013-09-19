package models.data;

import models.Contest;
import models.Event;
import models.User;
import models.newserialization.SerializableUpdatable;
import play.mvc.Call;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 18:12
 */
public interface ObjectsProviderFactory<T> extends SerializableUpdatable {

    ObjectsProvider<T> get(Event currentEvent, User currentUser);

    Class<T> getObjectsClass();

}
