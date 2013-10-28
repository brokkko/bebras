package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import controllers.Email;
import controllers.MongoConnection;
import models.newserialization.*;
import org.apache.commons.mail.EmailException;
import org.bson.types.ObjectId;
import play.Logger;
import play.mvc.Call;
import play.mvc.Http;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 28.10.13
 * Time: 17:53
 */
public class Announcement implements SerializableUpdatable {

    private ObjectId id;
    private String subject;
    private String message;
    private UserRole role;
    private Event event;
    private boolean sent;

    public Announcement() {
    }

    public Announcement(String subject, String message, UserRole role, Event event) {
        this.id = new ObjectId();

        this.subject = subject;
        this.message = message;
        this.role = role;
        this.event = event;
        this.sent = false;
    }

    public void store() {
        MongoSerializer mongo = new MongoSerializer();
        serialize(mongo);
        MongoConnection.getMailingListCollection().save(mongo.getObject());
    }

    @Override
    public void serialize(Serializer serializer) {
        if (id != null)
            serializer.write("_id", id);
        serializer.write("event_id", event.getId());
        serializer.write("message", message);
        serializer.write("subject", subject);
        serializer.write("role", role.getName());
        serializer.write("sent", sent);
    }

    @Override
    public void update(Deserializer deserializer) {
        id = deserializer.readObjectId("_id");
        message = deserializer.readString("message");
        subject = deserializer.readString("subject");
        event = Event.getInstance(deserializer.readString("event_id"));
        if (event != null)
            role = event.getRole(deserializer.readString("role"));
        sent = deserializer.readBoolean("sent");
    }

    public void sendTestMail() throws EmailException {
        User user = User.current();

        StringBuilder message = new StringBuilder();
        message.append("Здравствуйте");
        if (user.getGreeting() != null)
            message.append(", ").append(user.getGreeting());
        message.append("!\n\n");

        message.append(this.message).append("\n\n");
        message.append("--\nС уважением,\n    оргкомитет, ").append(Event.current().getTitle());
        message.append(" ").append(Email.getFrom());

        message.append("\n\n");
        message.append("Рассылка предназначена для: ").append(role.getTitle());
        message.append(". Если в сообщении всё правильно, вы можете послать его, для этого перейдите по ссылке: ");
        message.append(controllers.routes.Announcements.sendAnnouncement(event.getId(), getId().toString()).absoluteURL(Http.Context.current().request()));
        message.append(". Чтобы исправить сообщение, перейдите по ссылке: ");
        message.append(controllers.routes.Announcements.fixAnnouncement(event.getId(), getId().toString()).absoluteURL(Http.Context.current().request()));
        message.append(".");

        Email.sendEmail(user.getEmail(), "Тестирование рассылки: " + subject, message.toString());
    }

    public void sendEmails() {
        try {
            sendEmailForUser(User.current());
        } catch (EmailException e) {
            Logger.warn("Failed to send email in mailing list: " + User.current().getLogin() + "<" + User.current().getEmail() + ">");
        }
    }

    private void sendEmailForUser(User user) throws EmailException {
        Call unsubscribeCall = controllers.routes.Application.setSubscription(event.getId(), user.getId().toString(), false);
        String unsubscribeLink = unsubscribeCall.absoluteURL(Http.Context.current().request());

        StringBuilder message = new StringBuilder();
        message.append("Здравствуйте");
        if (user.getGreeting() != null)
            message.append(", ").append(user.getGreeting());
        message.append("!\n\n");

        message.append(this.message).append("\n\n");
        message.append("--\nС уважением,\n    оргкомитет, ").append(Event.current().getTitle());
        message.append(" ").append(Email.getFrom());

        message.append("\n\n");
        message.append("Вы получили это письмо, потому что зарегистрированы как ").append(role.getTitle());
        message.append(" в событии ").append(event.getTitle()).append(". При регистрации использовался адрес ");
        message.append(user.getEmail()).append(". ");
        message.append("Чтобы отписаться от рассылки, перейдите по ссылке: ");
        message.append(unsubscribeLink);
        message.append(".");

        Email.sendEmail(user.getEmail(), subject, message.toString(), null, unsubscribeLink);
    }

    public static Announcement getInstance(String aid) {
        ObjectId id;
        try {
            id = new ObjectId(aid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        DBObject object = MongoConnection.getMailingListCollection().findOne(new BasicDBObject("_id", id));
        if (object == null)
            return null;

        Announcement result = new Announcement();
        result.update(new MongoDeserializer(object));
        return result;
    }

    public ObjectId getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getSubject() {
        return subject;
    }

    public boolean isSent() {
        return sent;
    }

    public UserRole getRole() {
        return role;
    }

    public Event getEvent() {
        return event;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
