package models.data;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.05.13
 * Time: 21:51
 */
public class CsvDataWriter<T> implements AutoCloseable {

    private List<Feature<T>> features = new ArrayList<>();
    private CSVWriter out;
    private boolean headerWritten = false;

    public CsvDataWriter(OutputStream out) throws UnsupportedEncodingException {
        this.out = new CSVWriter(new OutputStreamWriter(out, "windows-1251"), ';', '"');
    }

    public void addFeature(Feature<T> feature) {
        features.add(feature);
    }

    public void writeObject(T object) {
        if (!headerWritten) {
            headerWritten = true;

            String[] newLine = new String[features.size()];
            int ind = 0;
            for (Feature<T> feature : features)
                newLine[ind++] = feature.name();
            out.writeNext(newLine);
        }

        String[] newLine = new String[features.size()];
        int ind = 0;
        for (Feature<T> feature : features)
            newLine[ind++] = feature.eval(object);
        out.writeNext(newLine);
    }

    @Override
    public void close() throws Exception {
        out.close();
    }

}
