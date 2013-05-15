package models.data;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.05.13
 * Time: 21:48
 */
public interface Feature<T> {

    String name();
    String eval(T object);

}