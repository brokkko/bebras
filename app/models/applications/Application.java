package models.applications;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.09.13
 * Time: 15:07
 */
public class Application implements SerializableUpdatable {

    public static int NEW = 0;
    public static int PAYED = 1;
    public static int CONFIRMED = 2;

    private String name;
    private int size;
    private int state;
    private int number;
    private Date created;
    private String comment;
    private boolean kio;

    public Application() {
    }

    private String hexBytes(int x, int halfBytes) {
        String s = Integer.toHexString(x).toUpperCase();
        while (s.length() < halfBytes)
            s = '0' + s;
        int len = s.length();
        if (len > halfBytes)
            s = s.substring(len - halfBytes);
        return s;
    }

    //TODO name generation may produce colliding ids
    public Application(User organizer, int size, int number, boolean kio) {
        int inc = organizer.getId().getInc();
        int machine = organizer.getId().getMachine();
        String code = hexBytes(machine, 2) + hexBytes(inc, 4);

        this.name = organizer.getInfo().get("region") + "-" + code + "-" + number + (kio ? "bk" : "b");
        this.number = number;
        this.size = size;
        this.kio = kio;
        this.created = new Date();
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getCreated() {
        return created;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getNumber() {
        return number;
    }

    public int getPrice() {
        return size * (kio ? 100 : 50);
    }

    public boolean isKio() {
        return kio;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        serializer.write("size", size);
        serializer.write("state", state);
        serializer.write("number", number);
        serializer.write("created", created);
        serializer.write("comment", comment);
        serializer.write("kio", kio);
    }

    @Override
    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        size = deserializer.readInt("size");
        state = deserializer.readInt("state");
        number = deserializer.readInt("number");
        created = deserializer.readDate("created");
        comment = deserializer.readString("comment");
        kio = deserializer.readBoolean("kio");
    }
}
