package models.data;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 14:50
 */
public class CompositeFeaturesSet<T> implements FeaturesSet<T> {

    private final String delimiter;
    private Map<String, FeaturesSet<T>> name2set = new LinkedHashMap<>();
    private Set<FeaturesSet<T>> loaded = new HashSet<>();
    private T loadedObject;

    public CompositeFeaturesSet() {
        this(null);
    }

    public CompositeFeaturesSet(String delimiter) {
        this.delimiter = delimiter;
    }

    public void register(String name, FeaturesSet<T> set) {
        name2set.put(name, set);
    }

    @Override
    public void load(T object) throws Exception {
        loaded.clear();

        loadedObject = object;

        // for null delimiter load all right now, for other delimiter load features only when needed
        if (delimiter == null)
            for (FeaturesSet<T> set : name2set.values())
                loadFeatureSet(set);
    }

    private void loadFeatureSet(FeaturesSet<T> set) throws Exception {
        try {
            set.load(loadedObject);
            loaded.add(set);
        } catch (Exception e) {
            //close all loaded
            for (FeaturesSet<T> loadedFeature: loaded)
                try {
                    loadedFeature.close();
                } catch (Exception ee) {
                    e.addSuppressed(ee);
                }

            throw e;
        }
    }

    @Override
    public Object getFeature(String featureName) throws Exception {
        if (delimiter == null)
            return getFeatureWithoutDelimiter(featureName);
        else
            return getFeatureWithDelimiter(featureName);
    }

    private Object getFeatureWithDelimiter(String featureName) throws Exception {
        String[] split = splitName(featureName);

        FeaturesSet<T> set = name2set.get(split[0]);

        if (set == null)
            throw new IllegalArgumentException("No subset with name " + split[0]);

        if (! loaded.contains(set))
            loadFeatureSet(set);

        return set.getFeature(split[1]);
    }

    private Object getFeatureWithoutDelimiter(String featureName) throws Exception {
        for (FeaturesSet<T> set : name2set.values()) {
            Object result = set.getFeature(featureName);
            if (result != null)
                return result;
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        Exception closingException = null;

        for (FeaturesSet<T> featuresSet : loaded)
            try {
                featuresSet.close();
            } catch (Exception e) {
                if (closingException == null)
                    closingException = e;
                else
                    closingException.addSuppressed(e);
            }

        if (closingException != null)
            throw closingException;
    }

    private String[] splitName(String featureName) {
        int pos = featureName.indexOf(delimiter);
        if (pos < 0)
            throw new IllegalArgumentException("Can not find delimiter " + delimiter + " in name " + featureName);

        String name = featureName.substring(0, pos);
        String feature = featureName.substring(pos + delimiter.length());

        return new String[]{name, feature};
    }
}
