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

    private String title;
    private ObjectsProviderFactory<T> objectsProviderFactory;
    private List<String> titles;
    private List<String> featureNames;
    private String right;
    private String filename;
    private boolean showAsTable; //false by default

    public TableDescription() {
    }

    public String getTitle() {
        return title;
    }

    public ObjectsProviderFactory<T> getObjectsProviderFactory() {
        return objectsProviderFactory;
    }

    public Table<T> getTable() {
        FeaturesSet<T> featuresSet = FeaturesSetRegistry.getInstance().getFeaturesSet(objectsProviderFactory.getObjectsClass());

        return new Table<>(titles, featureNames, featuresSet);
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

        this.filename = deserializer.readString("filename");

        this.showAsTable = deserializer.readBoolean("show as table", false);
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

        if (filename != null)
            serializer.write("filename", filename);

        if (showAsTable)
            serializer.write("show as table", showAsTable);
    }
}