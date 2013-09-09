package models.newserialization;

import org.bson.types.ObjectId;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;

/**
 * Created by ilya
 */
public abstract class ListSerializer {

    public void write(byte value) {
        write((int) value);
    }

    public void write(short value) {
        write((int) value);
    }

    public abstract void write(int value);

    public abstract void write(long value);

    public void write(float value) {
        write((double) value);
    }

    public abstract void write(double value);

    public abstract void write(boolean value);

    public abstract void write(String value);

    public void write(Date value) {
        write(value == null ? null : DateSerializationUtils.dateToString(value));
    }

    public void write(ObjectId value) {
        write(value == null ? null : value.toString());
    }

    public void write(byte[] value) {
        write(value == null ? null : DatatypeConverter.printBase64Binary(value));
    }

    public void write(char value) {
        write("" + value);
    }

    public abstract Serializer getSerializer();

    public abstract ListSerializer getListSerializer();

    public void write(Serializable value) {
        if (value == null) {
            write((String) null); //it does not matter what null to write
            return;
        }

        Serializer serializer = getSerializer();
        if (serializer != null)
            value.serialize(serializer);
    }

    public void writeNull() {
        write((String) null); //String type does not matter
    }
}
