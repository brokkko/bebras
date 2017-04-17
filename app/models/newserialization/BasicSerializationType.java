package models.newserialization;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by ilya
 */
public class BasicSerializationType<T> extends SerializationType<T> {

    //TODO make static constants for types

    private final String className;

    public BasicSerializationType(Class<T> clazz) {
        String className = clazz.getCanonicalName();

        //convert basic class wrappers to basic classes
        switch (className) {
            case "java.lang.Byte": className = "byte"; break;
            case "java.lang.Short": className = "short"; break;
            case "java.lang.Integer": className = "int"; break;
            case "java.lang.Long": className = "long"; break;
            case "java.lang.Character": className = "char"; break;
            case "java.lang.Float": className = "float"; break;
            case "java.lang.Double": className = "double"; break;
            case "java.lang.Boolean": className = "boolean"; break;
        }

        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public void write(Serializer serializer, String field, T value) {
        if (value == null) {
            serializer.writeNull(field);
            return;
        }

        switch (className) {
            case "byte": serializer.write(field, (Byte) value); break;
            case "short": serializer.write(field, (Short) value); break;
            case "int": serializer.write(field, (Integer) value); break;
            case "long": serializer.write(field, (Long) value); break;
            case "float": serializer.write(field, (Float) value); break;
            case "double": serializer.write(field, (Double) value); break;
            case "char": serializer.write(field, (Character) value); break;
            case "boolean": serializer.write(field, (Boolean) value); break;
            case "java.lang.String": serializer.write(field, (String) value); break;
            case "java.util.Date": serializer.write(field, (Date) value); break;
            case "org.bson.types.ObjectId": serializer.write(field, (ObjectId) value); break;
            case "byte[]": serializer.write(field, (byte[]) value); break;
        }
    }

    @Override
    public void write(ListSerializer serializer, T value) {
        if (value == null) {
            serializer.write((String) null);
            return;
        }

        switch (className) {
            case "byte": serializer.write((Byte) value); break;
            case "short": serializer.write((Short) value); break;
            case "int": serializer.write((Integer) value); break;
            case "long": serializer.write((Long) value); break;
            case "float": serializer.write((Float) value); break;
            case "double": serializer.write((Double) value); break;
            case "char": serializer.write((Character) value); break;
            case "boolean": serializer.write((Boolean) value); break;
            case "java.lang.String": serializer.write((String) value); break;
            case "java.util.Date": serializer.write((Date) value); break;
            case "org.bson.types.ObjectId": serializer.write((ObjectId) value); break;
            case "byte[]": serializer.write((byte[]) value); break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(Deserializer deserializer, String field) {
        switch (className) {
            case "byte": return (T) deserializer.readInt(field);
            case "short": return (T) deserializer.readShort(field);
            case "int": return (T) deserializer.readInt(field);
            case "long": return (T) deserializer.readLong(field);
            case "float": return (T) deserializer.readFloat(field);
            case "double": return (T) deserializer.readDouble(field);
            case "char": return (T) deserializer.readChar(field);
            case "boolean": return (T) deserializer.readBoolean(field);
            case "java.lang.String": return (T) deserializer.readString(field);
            case "java.util.Date": return (T) deserializer.readDate(field);
            case "org.bson.types.ObjectId": return (T) deserializer.readObjectId(field);
            case "byte[]": return (T) deserializer.readByte(field);
        }
        
        throw new IllegalArgumentException("Unknown type " + className);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(ListDeserializer deserializer) {
        switch (className) {
            case "byte": return (T) deserializer.readInt();
            case "short": return (T) deserializer.readShort();
            case "int": return (T) deserializer.readInt();
            case "long": return (T) deserializer.readLong();
            case "float": return (T) deserializer.readFloat();
            case "double": return (T) deserializer.readDouble();
            case "char": return (T) deserializer.readChar();
            case "boolean": return (T) deserializer.readBoolean();
            case "java.lang.String": return (T) deserializer.readString();
            case "java.util.Date": return (T) deserializer.readDate();
            case "org.bson.types.ObjectId": return (T) deserializer.readObjectId();
            case "byte[]": return (T) deserializer.readByte();
        }

        return null;
    }
}
