package models.data;

import models.newserialization.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 17:26
 */
public class TableDescription<T> implements SerializableUpdatable {

    public static final String HTML_POSTFIX = "<html>";
    public static final String CSV_POSTFIX = "<csv>";

    private String title;
    private String comment;
    private ObjectsProviderFactory<T> objectsProviderFactory;
    private List<String> titles;
    private List<String> featureNames;
    private String right;
    private String filename;
    private boolean showAsTable; //false by default
    private boolean showSearch;
    private int maxViewSize;

    public TableDescription() {
    }

    public String getTitle() {
        return title;
    }

    public ObjectsProviderFactory<T> getObjectsProviderFactory() {
        return objectsProviderFactory;
    }

    public Table<T> getTable(FeaturesContext context) {
        FeaturesSet<T> featuresSet = FeaturesSetRegistry.getInstance().getFeaturesSet(objectsProviderFactory.getObjectsClass());

        List<String> filteredTitles = new ArrayList<>();
        List<String> filteredFeatureNames = new ArrayList<>();

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);

            //check HTML_POSTFIX and remove if needed
            if (title.endsWith(HTML_POSTFIX)) {
                if (context.getType() != FeaturesContestType.INTERFACE)
                    continue;
                title = title.substring(0, title.length() - HTML_POSTFIX.length());
            }

            //check CSV_POSTFIX and remove if needed
            if (title.endsWith(CSV_POSTFIX)) {
                if (context.getType() != FeaturesContestType.CSV)
                    continue;
                title = title.substring(0, title.length() - CSV_POSTFIX.length());
            }

            filteredTitles.add(title);
            filteredFeatureNames.add(featureNames.get(i));
        }

        return new Table<>(filteredTitles, filteredFeatureNames, featuresSet, context);
    }

    public String getComment() {
        return comment;
    }

    public String getRight() {
        return right;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isShowAsTable() {
        return showAsTable;
    }

    public boolean isShowSearch() {
        return showSearch;
    }

    public int getMaxViewSize() {
        return maxViewSize;
    }

    @Override
    public void update(Deserializer deserializer) {
        this.title = deserializer.readString("title");

        //noinspection unchecked
        this.objectsProviderFactory = (ObjectsProviderFactory<T>)
                SerializationTypesRegistry.OBJECTS_PROVIDER_FACTORY.read(deserializer, "objects");

        this.right = deserializer.readString("right", "event admin");

        //read table

        titles = new ArrayList<>();
        featureNames = new ArrayList<>();

        ListDeserializer columnsDeserializer = deserializer.getListDeserializer("columns");

        while (columnsDeserializer.hasMore()) {
            String columnDescription = columnsDeserializer.readString();
            //parse column description and add it
            String delimiter = "->";

            int pos = columnDescription.indexOf(delimiter);

            String title = columnDescription.substring(0, pos).trim();
            String feature = columnDescription.substring(pos + delimiter.length()).trim();

            titles.add(title);
            featureNames.add(feature);
        }

        this.comment = deserializer.readString("comment");

        this.filename = deserializer.readString("filename");

        this.showAsTable = deserializer.readBoolean("show as table", false);

        this.showSearch = deserializer.readBoolean("show search", true);

        this.maxViewSize = deserializer.readInt("max view size", 500);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
        serializer.write("right", right);

        SerializationTypesRegistry.OBJECTS_PROVIDER_FACTORY.write(serializer, "objects", objectsProviderFactory);

        //write table
        ListSerializer columnsSerializer = serializer.getListSerializer("columns");

        int n = titles.size();
        for (int i = 0; i < n; i++) {
            String title = titles.get(i);
            String featureName = featureNames.get(i);

            columnsSerializer.write(title + " -> " + featureName);
        }

        if (comment != null)
            serializer.write("comment", comment);

        if (filename != null)
            serializer.write("filename", filename);

        if (showAsTable)
            serializer.write("show as table", showAsTable);

        if (!showSearch)
            serializer.write("show search", showSearch);

        serializer.write("max view size", maxViewSize);
    }
}