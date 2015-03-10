package plugins.kio.logs;

import java.io.UnsupportedEncodingException;

public class ByteArray {

    private byte[] array;
    private int pos = 0;

    public ByteArray(byte[] array) {
        this.array = array;
    }

    public int bytesAvailable() {
        return array.length - pos;
    }

    public double readByte() {
        return array[pos++];
    }

    public int readUnsignedByte() {
        int b = array[pos++];
        if (b < 0)
            return b + 256;
        else
            return b;
    }

    public int readInt() {
        int a = readUnsignedByte();
        int b = readUnsignedByte();
        int c = readUnsignedByte();
        int d = readUnsignedByte();

        return a << 24 | b << 16 | c << 8 | d;
    }

    public long readUnsignedInt() {
        long a = readUnsignedByte();
        long b = readUnsignedByte();
        long c = readUnsignedByte();
        long d = readUnsignedByte();

        return a << 24 | b << 16 | c << 8 | d;
    }

    public long readLong() {
        return readUnsignedInt() << 32 | readUnsignedInt();
    }

    public short readShort() {
        return (short)readUnsignedShort();
    }

    public int readUnsignedShort() {
        int a = readUnsignedByte();
        int b = readUnsignedByte();

        return a << 8 | b;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public String readUTF() {
        int len = readUnsignedShort();
        try {
            String result = new String(array, pos, len, "UTF-8");
            pos += len;
            return result;
        } catch (UnsupportedEncodingException e) {
            return "UNSUPPORTED ENCODING";
        }
    }
}
