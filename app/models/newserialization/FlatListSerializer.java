package models.newserialization;

import models.forms.RawForm;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by ilya
 */
public class FlatListSerializer extends ListSerializer {

    private final RawForm rawForm;
    private final String prefix;
    private final String delimiter;
    int index = 0;

    public FlatListSerializer(String delimiter) {
        this.rawForm = new RawForm();
        this.delimiter = delimiter;
        this.prefix = "";
    }

    /*internal*/ FlatListSerializer(RawForm rawForm, String prefix, String delimiter) {
        this.rawForm = rawForm;
        this.prefix = prefix;
        this.delimiter = delimiter;
    }

    public RawForm getRawForm() {
        return rawForm;
    }

    // implement Serializer

    private void writeObject(Object value) {
        rawForm.put(prefix + (index++), value == null ? null : value.toString());
    }

    @Override
    public void write(int value) {
        writeObject(value);
    }
    @Override
    public void write(long value) {
        writeObject(value);
    }

    @Override
    public void write(double value) {
        writeObject(value);
    }

    @Override
    public void write(boolean value) {
        writeObject(value);
    }

    @Override
    public void write(String value) {
        writeObject(value);
    }

    @Override
    public Serializer getSerializer() {
        return new FlatSerializer(rawForm, prefix + (index++) + delimiter, delimiter);
    }

    @Override
    public ListSerializer getListSerializer() {
        return new FlatListSerializer(rawForm, prefix + (index++) + delimiter, delimiter);
    }

    // override instead of implement

    @Override
    public void write(byte value) {
        writeObject( value);
    }

    @Override
    public void write(short value) {
        writeObject(value);
    }

    @Override
    public void write(float value) {
        writeObject(value);
    }

    @Override
    public void write(Date value) {
        writeObject(value);
    }

    @Override
    public void write(ObjectId value) {
        writeObject(value);
    }

    @Override
    public void write(byte[] value) {
        writeObject(value);
    }

    @Override
    public void write(char value) {
        writeObject(value);
    }

}
