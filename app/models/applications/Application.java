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
    private Date created;
    private String comment;
    private boolean kio;

    public Application() {
    }

    public Application(User organizer, int number, boolean kio) {
        this.name = organizer.getInfo().get("region") + "-" + organizer.getLogin() + "-" + number;
        this.kio = kio;
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

    public Date getCreated() {
        return created;
    }

    public String getComment() {
        return comment;
    }

    public int getNumber() {
        int pos = name.lastIndexOf('-');
        if (pos < 0)
            return 0;
        try {
            return Integer.parseInt(name.substring(pos + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        serializer.write("size", size);
        serializer.write("state", state);
        serializer.write("created", created);
        serializer.write("comment", comment);
        serializer.write("kio", kio);
    }

    @Override
    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        size = deserializer.readInt("size");
        state = deserializer.readInt("state");
        created = deserializer.readDate("created");
        comment = deserializer.readString("comment");
        kio = deserializer.readBoolean("kio");
    }
}
