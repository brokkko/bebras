package app.models.newserialization;

import models.newserialization.*;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Date;

/**
 * Created by ilya
 */
public class TestSerializers {

    private static final Date NOW = new Date();
    private static final byte[] DATA = new byte[]{4, 2, 3, 9, -123};

    @Test
    public void testAllSerializersDeserializers() {
        //mongo
        MongoSerializer outM = new MongoSerializer();
        MongoDeserializer inM = new MongoDeserializer(outM.getObject());

        testSerialization(outM, inM);

        //memory
        MemorySerializer outMM = new MemorySerializer();
        MemoryDeserializer inMM = new MemoryDeserializer(outMM.getMap());

        testSerialization(outMM, inMM);

        //json
        JSONSerializer outJ = new JSONSerializer();
        JSONDeserializer inJ = new JSONDeserializer(outJ.getNode());

        testSerialization(outJ, inJ);
    }

    private void testSerialization(Serializer out, Deserializer in) {
        Serializer sub1 = out.getSerializer("s1");
        ListSerializer sub2 = out.getListSerializer("s2");

        Serializer sub11 = sub1.getSerializer("s11");
        ListSerializer sub12 = sub1.getListSerializer("s12");

        Serializer sub21 = sub2.getSerializer();
        ListSerializer sub22 = sub2.getListSerializer();

        writeToSerializer(out, "p-");
        writeToSerializer(sub1, "p1-");
        writeToListSerializer(sub2);
        writeToSerializer(sub11, "p11-");
        writeToListSerializer(sub12);
        writeToSerializer(sub21, "p21-");
        writeToListSerializer(sub22);

        // deserialize now
        Deserializer d1 = in.getDeserializer("s1");
        ListDeserializer d2 = in.getListDeserializer("s2");

        Deserializer d11 = d1.getDeserializer("s11");
        ListDeserializer d12 = d1.getListDeserializer("s12");

        Deserializer d21 = d2.getDeserializer();
        ListDeserializer d22 = d2.getListDeserializer();

        //test finally
        readFromDeserializerAndCheck(in, "p-");
        readFromDeserializerAndCheck(d1, "p1-");
        readFromListDeserializerAndCheck(d2);
        readFromDeserializerAndCheck(d11, "p11-");
        readFromListDeserializerAndCheck(d12);
        readFromDeserializerAndCheck(d21, "p21-");
        readFromListDeserializerAndCheck(d22);
    }

    private void writeToSerializer(Serializer out, String prefix) {
        out.write(prefix + "b", (byte) 42);
        out.write(prefix + "b-", (byte) -42);
        out.write(prefix + "s", (short) 10000);
        out.write(prefix + "s-", (short) -10000);
        out.write(prefix + "i", 57121);
        out.write(prefix + "l", 10000000000l);
        out.write(prefix + "f", 2.39f);
        out.write(prefix + "d", 57.121);
        out.write(prefix + "bool", false);
        out.write(prefix + "S", "hello");
        out.write(prefix + "D", NOW);
        out.write(prefix + "bb", DATA);
        out.write(prefix + "c", 'x');
        out.write(prefix + "n", (String) null);
    }

    private void readFromDeserializerAndCheck(Deserializer in, String prefix) {
        assertEquals(42, (long) in.readByte(prefix + "b"));
        assertEquals(-42, (long) in.readByte(prefix + "b-"));
        assertEquals(10000, (long) in.readShort(prefix + "s"));
        assertEquals(-10000, (long) in.readShort(prefix + "s-"));
        assertEquals(57121, (long) in.readInt(prefix + "i"));
        assertEquals(10000000000l, (long) in.readLong(prefix + "l"));
        assertEquals(2.39f, in.readFloat(prefix + "f"), 1e-3);
        assertEquals(57.121, in.readDouble(prefix + "d"), 1e-3);
        assertEquals(false, in.readBoolean(prefix + "bool"));
        assertEquals("hello", in.readString(prefix + "S"));
        assertEquals(NOW, in.readDate(prefix + "D"));
        assertArrayEquals(DATA, in.readByteArray(prefix + "bb"));
        assertEquals('x', (char) in.readChar(prefix + "c"));
        assertTrue(in.isNull(prefix + "n"));
        assertNull(in.readString(prefix + "n"));

        assertNull(in.readChar("-------------"));
    }

    private void writeToListSerializer(ListSerializer out) {
        out.write((byte) 42);
        out.write((byte) -42);
        out.write((short) 10000);
        out.write((short) -10000);
        out.write(57121);
        out.write(10000000000L);
        out.write(2.39f);
        out.write(57.121);
        out.write(false);
        out.write("hello");
        out.write(NOW);
        out.write(DATA);
        out.write('x');
        out.write((String) null);
    }

    private void readFromListDeserializerAndCheck(ListDeserializer in) {
        assertEquals(42, (long) in.readByte());
        assertEquals(-42, (long) in.readByte());
        assertEquals(10000, (long) in.readShort());
        assertEquals(-10000, (long) in.readShort());
        assertEquals(57121, (long) in.readInt());
        assertEquals(10000000000l, (long) in.readLong());
        assertEquals(2.39f, in.readFloat(), 1e-10);
        assertEquals(57.121, in.readDouble(), 1e-3);
        assertEquals(false, in.readBoolean());
        assertEquals("hello", in.readString());
        assertEquals(NOW, in.readDate());
        assertArrayEquals(DATA, in.readByteArray());
        assertEquals('x', (char) in.readChar());
        assertTrue(in.nextIsNull());
        assertNull(in.readString());

        assertFalse(in.hasMore());
    }

}
