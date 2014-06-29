package models.newserialization;

import org.bson.types.ObjectId;

import javax.xml.bind.DatatypeConverter;
import java.util.Collection;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.03.13
 * Time: 14:06
 */
public abstract class Deserializer {

    public Byte readByte(String field) {
        Integer result = readInt(field);
        return result == null ? null : result.byteValue();
    }

    public byte readByte(String field, byte defaultValue) {
        Byte result = readByte(field);
        return result == null ? defaultValue : result;
    }

    public Short readShort(String field) {
        Integer result = readInt(field);
        return result == null ? null : result.shortValue();
    }

    public short readShort(String field, short defaultValue) {
        Short result = readShort(field);
        return result == null ? defaultValue : result;
    }

    public abstract Integer readInt(String field);

    public int readInt(String field, int defaultValue) {
        Integer result = readInt(field);
        return result == null ? defaultValue : result;
    }

    public abstract Long readLong(String field);

    public Long readLong(String field, long defaultValue) {
        Long result = readLong(field);
        return result == null ? defaultValue : result;
    }

    public Float readFloat(String field) {
        Double result = readDouble(field);
        return result == null ? null : result.floatValue();
    }

    public Float readFloat(String field, float defaultValue) {
        Float result = readFloat(field);
        return result == null ? defaultValue : result;
    }

    public abstract Double readDouble(String field);

    public Double readDouble(String field, double defaultValue) {
        Double result = readDouble(field);
        return result == null ? defaultValue : result;
    }

    public abstract Boolean readBoolean(String field);

    public Boolean readBoolean(String field, boolean defaultValue) {
        Boolean result = readBoolean(field);
        return result == null ? defaultValue : result;
    }

    public Character readChar(String field) {
        String result = readString(field);
        return result == null ? null : result.charAt(0);
    }

    public Character readChar(String field, char defaultValue) {
        Character result = readChar(field);
        return result == null ? defaultValue : result;
    }

    public abstract String readString(String field);

    public String readString(String field, String defaultValue) {
        String result = readString(field);
        return result == null ? defaultValue : result;
    }

    public Date readDate(String field) {
        String result = readString(field);
        return result == null ? null : DateSerializationUtils.stringToDate(result);
    }

    //null default value means now
    public Date readDate(String field, Date defaultValue) {
        Date result = readDate(field);
        return result != null ? result : (
                defaultValue == null ? new Date() : defaultValue
        );
    }

    public ObjectId readObjectId(String field) {
        String value = readString(field);
        return value == null ? null : new ObjectId(value);
    }

    public ObjectId readObjectId(String field, ObjectId defaultValue) {
        ObjectId objectId = readObjectId(field);
        return objectId == null ? defaultValue : objectId;
    }

    public byte[] readByteArray(String field) {
        //by default byte array is encoded as BASE64 String
        String text = readString(field);
        return text == null ? null : DatatypeConverter.parseBase64Binary(text);
    }

    public byte[] readByteArray(String field, byte[] defaultValue) {
        byte[] result = readByteArray(field);
        return result == null ? defaultValue : result;
    }

    public abstract Deserializer getDeserializer(String field);

    public abstract ListDeserializer getListDeserializer(String field);

    public <T extends Updatable> T update(String field, T updatable) {
        Deserializer deserializer = getDeserializer(field);
        if (deserializer != null)
            updatable.update(deserializer);
        else
            return null;
        return updatable;
    }

    public abstract Collection<String> fields();

    public boolean hasField(String field) {
        return fields().contains(field);
    }

    public abstract boolean isNull(String field);
}
