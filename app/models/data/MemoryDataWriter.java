package models.data;

import play.api.templates.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.05.13
 * Time: 21:51
 */
public class MemoryDataWriter<T> implements AutoCloseable {

    public static final int MAX_SIZE = 200;

    private final Table<T> table;
    private String fullTextSearch;
    private boolean inside;

    private final List<String[]> list = new ArrayList<>(MAX_SIZE);

    public MemoryDataWriter(Table<T> table, String fullTextSearch, boolean inside) {
        this.table = table;
        this.fullTextSearch = fullTextSearch == null ? null : fullTextSearch.toLowerCase();
        this.inside = inside;
    }

    private void writeObject(T object, FeaturesContext context) throws Exception {
        table.load(object);

        String[] newLine = new String[table.getFeaturesCount()];
        int ind = 0;
        boolean needLine = fullTextSearch == null;
        for (String feature : table.getFeatureNames()) {
            Object value = table.getFeature(feature, context);
            newLine[ind++] = value == null ? "-" : (
                    value instanceof Html ? value.toString() : views.html.htmlfeatures.string2html.render(value.toString()).toString()
            );
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
        while (provider.hasNext() && list.size() < MAX_SIZE)
            writeObject(provider.next(), context);
    }

    @Override
    public void close() throws Exception {
    }

    public List<String[]> getList() {
        return list;
    }
}
