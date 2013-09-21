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
    private final List<String[]> list = new ArrayList<>(MAX_SIZE);

    public MemoryDataWriter(Table<T> table) {
        this.table = table;
    }

    private void writeObject(T object, FeaturesContext context) throws Exception {
        table.load(object);

        String[] newLine = new String[table.getFeaturesCount()];
        int ind = 0;
        for (String feature : table.getFeatureNames()) {
            Object value = table.getFeature(feature, context);
            newLine[ind++] = value == null ? "-" : (
                    value instanceof Html ? value.toString() : views.html.htmlfeatures.string2html.render(value.toString()).toString()
            );
        }

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
