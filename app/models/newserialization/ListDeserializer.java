package models.newserialization;

import org.bson.types.ObjectId;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.03.13
 * Time: 14:06
 */
public abstract class ListDeserializer {

    public Byte readByte() {
        Integer result = readInt();
        return result == null ? null : result.byteValue();
    }

    public byte readByte(byte defaultValue) {
        Byte result = readByte();
        return result == null ? defaultValue : result;
    }

    public Short readShort() {
        Integer result = readInt();
        return result == null ? null : result.shortValue();
    }

    public short readShort(short defaultValue) {
        Short result = readShort();
        return result == null ? defaultValue : result;
    }

    public abstract Integer readInt();

    public int readInt(int defaultValue) {
        Integer result = readInt();
        return result == null ? defaultValue : result;
    }

    public abstract Long readLong();

    public Long readLong(long defaultValue) {
        Long result = readLong();
        return result == null ? defaultValue : result;
    }

    public Float readFloat() {
        Double result = readDouble();
        return result == null ? null : result.floatValue();
    }

    public Float readFloat(float defaultValue) {
        Float result = readFloat();
        return result == null ? defaultValue : result;
    }

    public abstract Double readDouble();

    public Double readDouble(double defaultValue) {
        Double result = readDouble();
        return result == null ? defaultValue : result;
    }

    public abstract Boolean readBoolean();

    public Boolean readBoolean(boolean defaultValue) {
        Boolean result = readBoolean();
        return result == null ? defaultValue : result;
    }

    public Character readChar() {
        String result = readString();
        return result == null ? null : result.charAt(0);
    }

    public Character readChar(char defaultValue) {
        Character result = readChar();
        return result == null ? defaultValue : result;
    }

    public abstract String readString();

    public String readString(String defaultValue) {
        String result = readString();
        return result == null ? defaultValue : result;
    }

    public Date readDate() {
        String result = readString();
        return result == null ? null : DateSerializationUtils.stringToDate(result);
    }

    //null default value means now
    public Date readDate(Date defaultValue) {
        Date result = readDate();
        return result == null ? null : (
                defaultValue == null ? new Date() : defaultValue
        );
    }

    public ObjectId readObjectId() {
        String value = readString();
        return value == null ? null : new ObjectId(value);
    }

    public ObjectId readObjectId(ObjectId defaultValue) {
        ObjectId objectId = readObjectId();
        return objectId == null ? defaultValue : objectId;
    }

    public byte[] readByteArray() {
        //by default byte array is encoded as BASE64 String
        String text = readString();
        return text == null ? null : DatatypeConverter.parseBase64Binary(text);
    }

    public byte[] readByteArray(byte[] defaultValue) {
        byte[] result = readByteArray();
        return result == null ? defaultValue : result;
    }

    public abstract Deserializer getDeserializer();

    public abstract ListDeserializer getListDeserializer();

    public <T extends Updatable> T update(T updatable) {
        Deserializer deserializer = getDeserializer();
        if (deserializer != null)
            updatable.update(deserializer);
        return updatable;
    }

    public abstract boolean hasMore();

    public abstract boolean nextIsNull();
}
