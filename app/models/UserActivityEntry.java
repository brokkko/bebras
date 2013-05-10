package models;

import controllers.MongoConnection;
import models.serialization.Deserializer;
import models.serialization.MongoSerializer;
import models.serialization.Serializer;

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

    private String user;
    private String ip;
    private String ua;
    private Date date;

    public UserActivityEntry(String user, String ip, String ua, Date date) {
        this.ip = ip;
        this.ua = ua;
        this.date = date;
    }

    public static UserActivityEntry deserialize(Deserializer deserializer) {
        return UserActivityEntry.deserialize(
                deserializer.getString(FIELD_USER),
                deserializer
        );
    }

    public static UserActivityEntry deserialize(String user, Deserializer deserializer) {
        return new UserActivityEntry(
                user,
                deserializer.getString(FIELD_IP),
                deserializer.getString(FIELD_USER_AGENT),
                (Date) deserializer.getObject(FIELD_DATE)
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
        mongoSerializer.store(MongoConnection.getActivityCollection());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserActivityEntry that = (UserActivityEntry) o;

        return ip.equals(that.ip) && ua.equals(that.ua);

    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + ua.hashCode();
        return result;
    }
}
