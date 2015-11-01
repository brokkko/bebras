package models.applications;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import controllers.MongoConnection;
import models.Event;
import models.ServerConfiguration;
import models.User;
import models.UserRole;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import models.results.Info;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Http;
import plugins.ApplicationType;

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

    private static String decBytes(int x, int digits) {
        String s = Integer.toString(x);
        while (s.length() < digits)
            s = '0' + s;
        int len = s.length();
        if (len > digits)
            s = s.substring(len - digits);
        return s;
    }

    public static String getCodeForUserHex(User user) { //TODO remove Fffff
        int inc = user.getId().getCounter();
        int machine = user.getId().getMachineIdentifier();

        return hexBytes(machine, 2) + hexBytes(inc, 4);
    }

    public static String getCodeForUser(User user) {
        int inc = user.getId().getCounter();
        int machine = user.getId().getMachineIdentifier();

        return decBytes(machine, 1) + decBytes(inc, 6);
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

    private String type;

    public Application() {
    }

    //TODO name generation may produce colliding ids
    public Application(User organizer, int size, int number, String type, int state) {
        this.name = organizer.getInfo().get("region") + "-" + getCodeForUser(organizer) + "-" + number + type;
        this.number = number;
        this.size = size;
        this.type = type;
        this.created = new Date();
        this.state = state;
        this.comment = "";
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

    public String getCode() {
        Matcher matcher = CODE_PATTERN.matcher(name);
        if (!matcher.matches())
            return "";
        return matcher.group(1);
    }

    public String getType() {
        return type;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        serializer.write("size", size);
        serializer.write("state", state);
        serializer.write("number", number);
        serializer.write("created", created);
        serializer.write("comment", comment);
        serializer.write("type", type);
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
        type = deserializer.readString("type");
        logins = SerializationTypesRegistry.list(String.class).read(deserializer, "logins");
    }

    public boolean createUsers(Event event, User user, UserRole role, ApplicationType appType) {
        if (appType.isSelf())
            return true;

        String prefix = getCodeForUser(user).toLowerCase() + number + ".";
        int index = 1;
        int skipped = 0;
        int created = 0;

        while (created < size) {
            String login = prefix + index;
            index ++;

            String password = User.generatePassword();
            Info info = new Info();
            info.put(User.FIELD_LOGIN, login);
            info.put(User.FIELD_RAW_PASS, password);

            if (appType.getUserFlag() != null)
                info.put(appType.getUserFlag(), "+");

            User createdUser = event.createUser(password, role, info, user, true);
            if (createdUser == null) {
                skipped++;

                if (skipped == 100)
                    return false;

                continue;
            }

            logins.add(login);
            created ++;
        }

        if (Http.Context.current.get() != null) { //TODO this is a hack, because sending emails does not work without http context. Need to get context here or just a link to event
            try {
                sendEmailAboutCreatedUsers(user);
            } catch (Exception e) {
                Logger.error("Failed to send email with application confirmation", e);
            }
        }

        return true;
    }

    private void sendEmailAboutCreatedUsers(User user) throws EmailException {
        String title = Event.current().getTitle();
        String subject = Messages.get("mail.application_confirm.subject", title);

        String greeting = user.getGreeting();
        if (greeting == null)
            greeting = "";
        if (!greeting.isEmpty())
            greeting = ", " + greeting;

        int size = getSize();
        String message = Messages.get("mail.application_confirm.body", greeting, getName(), size, size == 1 ? "а" : "", title, "Мои участники",
                                             controllers.routes.Application.enter(Event.currentId()).absoluteURL(Http.Context.current().request()));

        ServerConfiguration.getInstance().getCurrentDomain().getMailer().sendEmail(user.getEmail(), subject, message);
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

    public void clearUsers() {
        logins.clear();
    }
}
