package models.data;

import java.util.List;

public class Table<T> {
    private final List<String> titles;
    private final List<String> featureNames;
    private final FeaturesSet<T> featuresSet;

    public Table(List<String> titles, List<String> featureNames, FeaturesSet<T> featuresSet) {
        this.titles = titles;
        this.featureNames = featureNames;
        this.featuresSet = featuresSet;
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

    public int getFeaturesCount() {
        return titles.size();
    }

    public Object getFeature(String featureName) throws Exception {
        return featuresSet.getFeature(featureName);
    }

    public void load(T object) throws Exception {
        featuresSet.load(object);
    }
}