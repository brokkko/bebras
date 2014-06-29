package models.newserialization;

import models.forms.RawForm;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by ilya
 */
public class FlatSerializer extends Serializer {

    private final RawForm rawForm;
    private final String prefix;
    private final String delimiter;

    public FlatSerializer(String delimiter) {
        this.rawForm = new RawForm();
        this.delimiter = delimiter;
        this.prefix = "";
    }

    /*internal*/ FlatSerializer(RawForm rawForm, String prefix, String delimiter) {
        this.rawForm = rawForm;
        this.prefix = prefix;
        this.delimiter = delimiter;
    }

    public RawForm getRawForm() {
        return rawForm;
    }

    // implement Serializer

    private void writeObject(String field, Object value) {
        rawForm.put(prefix + field, value == null ? null : value.toString());
    }

    @Override
    public void write(String field, int value) {
        writeObject(field, value);
    }
    @Override
    public void write(String field, long value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, double value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, boolean value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, String value) {
        writeObject(field, value);
    }

    @Override
    public Serializer getSerializer(String field) {
        return new FlatSerializer(rawForm, prefix + field + delimiter, delimiter);
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        return new FlatListSerializer(rawForm, prefix + field + delimiter, delimiter);
    }

    // override instead of implement

    @Override
    public void write(String field, byte value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, short value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, float value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, Date value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, ObjectId value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, byte[] value) {
        writeObject(field, value);
    }

    @Override
    public void write(String field, char value) {
        writeObject(field, value);
    }

    @Override
    public void writeNull(String field) {
        writeObject(field, null);
    }
}
