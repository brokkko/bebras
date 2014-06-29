package models.newserialization;

import org.bson.types.ObjectId;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;

/**
 * Created by ilya
 */
public abstract class Serializer {

    public void write(String field, byte value) {
        write(field, (int) value);
    }

    public void write(String field, short value) {
        write(field, (int) value);
    }

    public abstract void write(String field, int value);

    public abstract void write(String field, long value);

    public void write(String field, float value) {
        write(field, (double) value);
    }

    public abstract void write(String field, double value);

    public abstract void write(String field, boolean value);

    public abstract void write(String field, String value);

    public void write(String field, Date value) {
        write(field, value == null ? null : DateSerializationUtils.dateToString(value));
    }

    public void write(String field, ObjectId value) {
        write(field, value == null ? null : value.toString());
    }

    public void write(String field, byte[] value) {
        write(field, value == null ? null : DatatypeConverter.printBase64Binary(value));
    }

    public void write(String field, char value) {
        write(field, "" + value);
    }

    public void writeNull(String field) {
        write(field, (String) null); //it does not matter what null to write
    }

    public abstract Serializer getSerializer(String field);

    public abstract ListSerializer getListSerializer(String field);

    public void write(String field, Serializable value) {
        if (value == null) {
            writeNull(field);
            return;
        }

        Serializer serializer = getSerializer(field);
        if (serializer != null)
            value.serialize(serializer);
    }
}
