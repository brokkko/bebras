package models.data;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 14:11
 */
public interface FeaturesSet<T> extends AutoCloseable {

    void load(T object) throws Exception;

    Object getFeature(String featureName) throws Exception;
}