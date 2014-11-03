package models;

import controllers.MongoConnection;
import models.newserialization.Deserializer;
import models.newserialization.MongoSerializer;
import models.newserialization.Serializer;
import org.bson.types.ObjectId;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 11.05.13
 * Time: 0:34
 */
public class UserActivityEntry {

    public static String FIELD_USER = "u";
    public static String FIELD_IP = "ip";
    public static String FIELD_USER_AGENT = "ua";
    public static String FIELD_DATE = "d";

    private ObjectId user;
    private String ip;
    private String ua;
    private Date date;

    public Date getDate() {
        return date;
    }

    public String getUa() {
        return ua;
    }

    public String getIp() {
        return ip;
    }

    public ObjectId getUser() {
        return user;
    }

    public UserActivityEntry(ObjectId user, String ip, String ua, Date date) {
        this.user = user;
        this.ip = ip;
        this.ua = ua;
        this.date = date;
    }

    public static UserActivityEntry deserialize(Deserializer deserializer) {
        return UserActivityEntry.deserialize(
                deserializer.readObjectId(FIELD_USER),
                deserializer
        );
    }

    public static UserActivityEntry deserialize(ObjectId user, Deserializer deserializer) {
        return new UserActivityEntry(
                user,
                deserializer.readString(FIELD_IP),
                deserializer.readString(FIELD_USER_AGENT),
                deserializer.readDate(FIELD_DATE)
        );
    }

    public void store(Serializer serializer, boolean storeUser) {
        if (storeUser)
            serializer.write(FIELD_USER, user);
        serializer.write(FIELD_IP, ip);
        serializer.write(FIELD_USER_AGENT, ua);
        serializer.write(FIELD_DATE, date);
    }

    public void store() {
        MongoSerializer mongoSerializer = new MongoSerializer();
        store(mongoSerializer, true);
        MongoConnection.getActivityCollection().save(mongoSerializer.getObject());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserActivityEntry that = (UserActivityEntry) o;

        return !(ip != null ? !ip.equals(that.ip) : that.ip != null) && !(ua != null ? !ua.equals(that.ua) : that.ua != null);
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (ua != null ? ua.hashCode() : 0);
        return result;
    }
}
