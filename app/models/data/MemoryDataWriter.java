package models.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.05.13
 * Time: 21:51
 */
public class MemoryDataWriter<T> implements AutoCloseable {

    private final Table<T> table;
    private String fullTextSearch;
    private boolean inside;
    private int maxSize;

    private final List<Object[]> list;

    /**
     * creates memory data writer
     * @param table a table to process
     * @param fullTextSearch a string with a text to search
     * @param inside inside = true means that text to search should be contained in the actual text, false means
     *               it should be equal to the actual text
     * @param maxSize maximal size to have in memory, -1 means that it is not constrained
     */
    public MemoryDataWriter(Table<T> table, String fullTextSearch, boolean inside, int maxSize) {
        this.table = table;
        this.fullTextSearch = fullTextSearch == null ? null : fullTextSearch.toLowerCase();
        this.inside = inside;
        this.maxSize = maxSize;

        if (maxSize > 0)
            list = new ArrayList<>(maxSize);
        else
            list = new ArrayList<>();
    }

    private void writeObject(T object, FeaturesContext context) throws Exception {
        table.load(object);

        Object[] newLine = new Object[table.getFeaturesCount()];
        int ind = 0;
        boolean needLine = fullTextSearch == null;
        for (String feature : table.getFeatureNames()) {
            Object value = table.getFeature(feature);
            Object outputValue = value;

            if (value != null && value instanceof WrappedFeatureValue) {
                outputValue = ((WrappedFeatureValue) value).getOutputValue();
                value = ((WrappedFeatureValue) value).getValue();
            }

            newLine[ind++] = outputValue == null ? "-" : outputValue;

            if (!needLine && value != null) //TODO report great idea feature. fullTextSearch != null as a second condition is marked as always true!!!
            {
                String testValue = value.toString().toLowerCase();
                if (inside)
                    needLine = testValue.contains(fullTextSearch);
                else
                    needLine = testValue.equals(fullTextSearch);
            }
        }

        if (needLine)
            list.add(newLine);
    }

    public void writeObjects(ObjectsProvider<T> provider, FeaturesContext context) throws Exception {
        while (provider.hasNext() && list.size() < maxSize)
            writeObject(provider.next(), context);
    }

    @Override
    public void close() throws Exception {
    }

    public List<Object[]> getList() {
        return list;
    }
}
