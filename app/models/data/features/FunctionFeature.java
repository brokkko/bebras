package models.data.features;

import models.data.Feature;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.05.13
 * Time: 22:55
 */
public abstract class FunctionFeature<T> implements Feature<T> {

    private final List<Feature<T>> features;

    protected FunctionFeature(List<Feature<T>> features) {
        this.features = features;
    }

    @Override
    public String eval(T object) {
        String[] params = new String[features.size()];

        for (int i = 0; i < features.size(); i++) {
            Feature<T> feature = features.get(i);
            params[i] = feature.eval(object);
        }

        return evalFunction(params);
    }

    protected abstract String evalFunction(String[] params);
}
