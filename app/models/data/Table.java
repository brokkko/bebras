package models.data;

import java.util.List;

public class Table<T> {
    private final List<String> titles;
    private final List<String> featureNames;
    private final FeaturesSet<T> featuresSet;
    private final FeaturesContext context;

    public Table(List<String> titles, List<String> featureNames, FeaturesSet<T> featuresSet, FeaturesContext context) {
        this.titles = titles;
        this.featureNames = featureNames;
        this.featuresSet = featuresSet;
        this.context = context;
    }

    public void register(String title, String featureName) {
        titles.add(title);
        featureNames.add(featureName);
    }

    public List<? extends String> getTitles() {
        return titles;
    }

    public List<? extends String> getFeatureNames() {
        return featureNames;
    }

    public FeaturesContext getContext() {
        return context;
    }

    public int getFeaturesCount() {
        return titles.size();
    }

    public Object getFeature(String featureName) throws Exception {
        return featuresSet.getFeature(featureName, context);
    }

    public void load(T object) throws Exception {
        featuresSet.load(object);
    }
}