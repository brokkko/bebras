package models;

import com.mongodb.*;
import controllers.Email;
import controllers.MongoConnection;
import models.newserialization.*;
import org.apache.commons.mail.EmailException;
import org.bson.types.ObjectId;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.mvc.Call;
import play.mvc.Http;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 28.10.13
 * Time: 17:53
 */
public class Announcement implements SerializableUpdatable {

    private static final int SEND_DELAY = 10;

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
        Mailer mailer = ServerConfiguration.getInstance().getCurrentDomain().getMailer();

        StringBuilder message = new StringBuilder();
        message.append("Здравствуйте");
        if (user.getGreeting() != null)
            message.append(", ").append(user.getGreeting());
        message.append("!\n\n");

        message.append(this.message).append("\n\n");
        message.append("--\nС уважением,\n    оргкомитет, ").append(Event.current().getTitle());
        message.append(" ").append(mailer.getReplyTo());

        message.append("\n\n");
        message.append("Рассылка предназначена для: ").append(role.getTitle());
        message.append(". Если в сообщении всё правильно, вы можете послать его, для этого перейдите по ссылке: ");
        message.append(controllers.routes.Announcements.sendAnnouncement(event.getId(), getId().toString()).absoluteURL(Http.Context.current().request()));
        message.append(". Чтобы исправить сообщение, перейдите по ссылке: ");
        message.append(controllers.routes.Announcements.fixAnnouncement(event.getId(), getId().toString()).absoluteURL(Http.Context.current().request()));
        message.append(".");

        mailer.sendEmail(user.getEmail(), "Тестирование рассылки: " + subject, message.toString());
    }

    public void sendEmails() {
        final User adminUser = User.current();
        final Call unsubscribePatternCall = controllers.routes.Application.setSubscription(event.getId(), "__USER__ID__", false);
        final String unsubscribePatternLink = unsubscribePatternCall.absoluteURL(Http.Context.current().request());

        Akka.system().scheduler().scheduleOnce(
                Duration.Zero(),
                new Runnable() {
                    public void run() {
                        boolean wasEmpty = queueIsEmpty();

                        appendToQueue(adminUser.getId(), adminUser.getLogin(), unsubscribePatternLink);

                        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());
                        query.put(User.FIELD_USER_ROLE, role.getName());
                        query.put(User.FIELD_ANNOUNCEMENTS, new BasicDBObject("$ne", false));
                        try (DBCursor cursor = MongoConnection.getUsersCollection().find(query)) {
                            while (cursor.hasNext()) {
                                DBObject next = cursor.next();
                                String login = (String) next.get(User.FIELD_LOGIN);
                                ObjectId uid = (ObjectId) next.get("_id");
                                appendToQueue(uid, login, unsubscribePatternLink);
                            }
                        } catch (Exception e) {
                            Logger.error("Failed to add users for mailing", e);
                        }

                        if (wasEmpty) //TODO not very good way to tell whether emailing scheduler is running
                            scheduleOneSending(1);
                    }
                },
                Akka.system().dispatcher()
        );
    }

    public static void scheduleOneSending(int seconds) {
        Akka.system().scheduler().scheduleOnce(
                Duration.create(seconds, TimeUnit.SECONDS),
                new Runnable() {
                    public void run() {
                        DBCollection mailingListQueueCollection = MongoConnection.getMailingListQueueCollection();
                        DBObject object = mailingListQueueCollection.findOne(new BasicDBObject(), new BasicDBObject(), new BasicDBObject("_id", 1));
                        if (object == null)
                            return;
                        mailingListQueueCollection.remove(object);

                        String login = (String) object.get("login");
                        ObjectId annId = (ObjectId) object.get("ann_id");
                        Announcement announcement = getInstance(annId);

                        if (announcement == null) {
                            Logger.warn("Unknown announcement while mailing: " + annId);
                            return;
                        }

                        String eventId = announcement.getEvent().getId();
                        Domain domain = Domain.getInstance(announcement.getEvent().getDomain());
                        Mailer mailer = domain.getMailer(); //TODO store domain in Announcement

                        User user = User.getInstance(User.FIELD_LOGIN, login, eventId);

                        if (user == null) {
                            Logger.warn("Unknown user while mailing: " + login + " in event " + eventId);
                            return;
                        }

                        try {
                            announcement.sendEmailForUser(mailer, user, (String) object.get("un_lnk"));
                        } catch (EmailException e) {
                            Logger.error("Failed to send email for user " + login + " in event " + eventId, e);
                        }

                        scheduleOneSending(SEND_DELAY);
                    }
                },
                Akka.system().dispatcher()
        );
    }

    private boolean queueIsEmpty() {
        return MongoConnection.getMailingListQueueCollection().count() == 0;
    }

    private void appendToQueue(ObjectId uid, String login, String unsubscribePatternLink) {
        DBObject object = new BasicDBObject("login", login);
        object.put("ann_id", id);
        object.put("un_lnk", unsubscribePatternLink.replaceAll("__USER__ID__", uid.toString()));
        MongoConnection.getMailingListQueueCollection().save(object);
    }

    private void sendEmailForUser(Mailer mailer, User user, String unsubscribeLink) throws EmailException {
        StringBuilder message = new StringBuilder();
        message.append("Здравствуйте");
        if (user.getGreeting() != null)
            message.append(", ").append(user.getGreeting());
        message.append("!\n\n");

        message.append(this.message).append("\n\n");
        message.append("--\nС уважением,\n    оргкомитет, ").append(event.getTitle());
        message.append(" ").append(mailer.getReplyTo());

        message.append("\n\n");
        message.append("Вы получили это письмо, потому что зарегистрированы как ").append(role.getTitle());
        message.append(" в событии ").append(event.getTitle()).append(". При регистрации использовался адрес ");
        message.append(user.getEmail()).append(". ");
        message.append("Чтобы отписаться от рассылки, перейдите по ссылке: ");
        message.append(unsubscribeLink);
        message.append(".");

        String to = user.getEmail();

        if (Play.isDev())
            to = "iposov+" + to.replaceAll("@", "__") + "@gmail.com";

        mailer.sendEmail(to, subject, message.toString(), null, unsubscribeLink);
    }

    public static Announcement getInstance(String aid) {
        ObjectId id;
        try {
            id = new ObjectId(aid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        return getInstance(id);
    }

    private static Announcement getInstance(ObjectId id) {
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
