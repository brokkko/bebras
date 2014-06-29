package models.data;

import models.newserialization.SerializableUpdatable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.07.13
 * Time: 15:19
 */
public interface ObjectsProvider<T> extends AutoCloseable {

    boolean hasNext();

    T next();

}
