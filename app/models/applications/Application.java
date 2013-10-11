package models.applications;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import controllers.MongoConnection;
import models.Event;
import models.User;
import models.UserRole;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import models.results.Info;
import play.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.09.13
 * Time: 15:07
 */
public class Application implements SerializableUpdatable {

    private static final Pattern CODE_PATTERN = Pattern.compile(".*-([0-9a-zA-Z]+)-.*");

    private static String hexBytes(int x, int halfBytes) {
        String s = Integer.toHexString(x).toUpperCase();
        while (s.length() < halfBytes)
            s = '0' + s;
        int len = s.length();
        if (len > halfBytes)
            s = s.substring(len - halfBytes);
        return s;
    }

    public static String getCodeForUser(User user) {
        int inc = user.getId().getInc();
        int machine = user.getId().getMachine();

        return hexBytes(machine, 2) + hexBytes(inc, 4);
    }

    public static int NEW = 0;
    public static int PAYED = 1;

    public static int CONFIRMED = 2;
    private String name;
    private int size;
    private int state;
    private int number;
    private Date created;
    private String comment;
    private List<String> logins = new ArrayList<>();

    private boolean kio;

    public Application() {
    }

    //TODO name generation may produce colliding ids
    public Application(User organizer, int size, int number, boolean kio) {
        this.name = organizer.getInfo().get("region") + "-" + getCodeForUser(organizer) + "-" + number + (kio ? "bk" : "b");
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

    public String getCode() {
        Matcher matcher = CODE_PATTERN.matcher(name);
        if (!matcher.matches())
            return "";
        return matcher.group(1);
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
        SerializationTypesRegistry.list(String.class).write(serializer, "logins", logins);
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
        logins = SerializationTypesRegistry.list(String.class).read(deserializer, "logins");
    }

    public boolean createUsers(Event event, User user, UserRole role) {
        String prefix = getCodeForUser(user).toLowerCase() + number + ".";
        int index = 1;
        int skipped = 0;

        while (index <= size) {
            String login = prefix + index;
            index ++;

            String password = User.generatePassword();
            Info info = new Info();
            info.put(User.FIELD_LOGIN, login);
            info.put(User.FIELD_RAW_PASS, password);

            boolean userCreated = event.createUser(password, role, info, user);
            if (!userCreated) {
                skipped++;

                if (skipped == 100)
                    return false;
            }

            logins.add(login);
        }

        return true;
    }

    public boolean removeUsers(Event event) {
        BasicDBList dbList = new BasicDBList();
        dbList.addAll(logins);
        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());
        query.put(User.FIELD_LOGIN, new BasicDBObject("$in", dbList));

        try {
            MongoConnection.getUsersCollection().remove(query);
        } catch (MongoException exception) {
            Logger.warn("Failed to remove users from application", exception);
            return false;
        }

        logins = new ArrayList<>();

        return true;
    }
}
