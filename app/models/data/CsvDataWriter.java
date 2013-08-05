package models.data;

import au.com.bytecode.opencsv.CSVWriter;
import play.Logger;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.05.13
 * Time: 21:51
 */
public class CsvDataWriter<T> implements AutoCloseable {

    private final Table<T> table;
    private CSVWriter out;
    private boolean headerWritten = false;

    public CsvDataWriter(Table<T> table, OutputStream out) {
        this.table = table;

        try {
            init(out, "windows-1251", ';', '"');
        } catch (UnsupportedEncodingException e) {
            Logger.error("Error in CsvDataWriter", e);
        }
    }

    public CsvDataWriter(Table<T> table, OutputStream out, String encoding, char delimiter, char quote) throws UnsupportedEncodingException {
        this.table = table;

        init(out, encoding, delimiter, quote);
    }

    private void init(OutputStream out, String encoding, char delimiter, char quote) throws UnsupportedEncodingException {
        this.out = new CSVWriter(new OutputStreamWriter(out, encoding), delimiter, quote);
    }

    private void writeObject(T object) throws Exception {
        if (!headerWritten) {
            headerWritten = true;

            String[] newLine = new String[table.getFeaturesCount()];
            int ind = 0;
            for (String title : table.getTitles())
                newLine[ind++] = title;
            out.writeNext(newLine);
        }

        table.load(object);

        String[] newLine = new String[table.getFeaturesCount()];
        int ind = 0;
        for (String feature : table.getFeatureNames()) {
            Object value = table.getFeature(feature);
            newLine[ind++] = value == null ? "" : value.toString();
        }
        out.writeNext(newLine);
    }

    public void writeObjects(ObjectsProvider<T> provider) throws Exception {
        while (provider.hasNext())
            writeObject(provider.next());
    }

    @Override
    public void close() throws Exception {
        out.close();
    }
}
