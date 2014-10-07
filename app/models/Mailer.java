package models;

import models.forms.InputForm;
import models.newserialization.Deserializer;
import models.newserialization.MemoryDeserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;
import models.utils.Utils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import play.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;

public class Mailer implements SerializableUpdatable {

    public static final InputForm MAILER_CHANGE_FORM = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "host",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "SMTP host",
                                            "placeholder", "Введите host"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "port",
                                    "view", Utils.mapify(
                                            "type", "int",
                                            "placeholder", "Введите порт",
                                            "title", "SMTP порт"
                                    ),
                                    "validators", Utils.listify(
                                            Utils.mapify(
                                                    "type", "int",
                                                    "compare", ">=0"
                                            )
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "ssl",
                                    "view", Utils.mapify(
                                            "type", "boolean",
                                            "title", "SSL"
                                    ),
                                   "required", true
                            ),
                            Utils.mapify(
                                    "name", "tls",
                                    "view", Utils.mapify(
                                            "type", "boolean",
                                            "title", "TLS"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "login",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Логин",
                                            "placeholder", "Введите логин"
                                    ),
                                    "required", false
                            ),
                            Utils.mapify(
                                    "name", "password",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Пароль",
                                            "placeholder", "Введите пароль"
                                    ),
                                    "required", false
                            ),
                            Utils.mapify(
                                    "name", "from",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "От кого (email)",
                                            "placeholder", "Введите email отправителя"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "fromName",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "От кого (имя)",
                                            "placeholder", "Введите имя отправителя"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "replyTo",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Обратный адрес",
                                            "placeholder", "Введите обратный адрес"
                                    ),
                                    "required", true
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    private String host;
    private int port;
    private boolean ssl;
    private boolean tls;
    private String login;
    private String password;
    private String from;
    private String fromName;
    private String replyTo;

    public String getReplyTo() {
        return replyTo;
    }

    private void prepareEmail(String to, String subject, Email email) throws EmailException {
        email.setHostName(host);
        email.setSmtpPort(port);

        email.setSSL(ssl);
        email.setTLS(tls);

        if (login != null && !login.isEmpty() && password != null && !password.isEmpty())
            email.setAuthentication(login, password);

        email.addTo(to);

        if (fromName == null || fromName.isEmpty())
            email.setFrom(from);
        else
            email.setFrom(from, fromName);

        if (replyTo != null && !replyTo.isEmpty())
            try {
                email.setReplyTo(Arrays.asList(new InternetAddress(replyTo)));
            } catch (AddressException e) {
                Logger.error("Failed to make Internet address out of " + replyTo);
            }

        email.setCharset("UTF8");
        email.setSubject(subject);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("host", host);
        serializer.write("port", port);
        serializer.write("ssl", ssl);
        serializer.write("tls", tls);
        serializer.write("login", login);
        serializer.write("password", password);
        serializer.write("from", from);
        serializer.write("fromName", fromName);
        serializer.write("replyTo", replyTo);
    }

    @Override
    public void update(Deserializer deserializer) {
        host = deserializer.readString("host", "");
        port = deserializer.readInt("port", 25);
        ssl = deserializer.readBoolean("ssl", false);
        tls = deserializer.readBoolean("tls", false);
        login = deserializer.readString("login", "");
        password = deserializer.readString("password", "");
        from = deserializer.readString("from", "");
        fromName = deserializer.readString("fromName", "");
        replyTo = deserializer.readString("replyTo", "");
    }

    public String sendEmail(String to, String subject, String message) throws EmailException {
        return sendEmail(to, subject, message, null, null);
    }

    public String sendEmail(String to, String subject, String message, String htmlMessage, String listUnsubscribe) throws EmailException {
        boolean isHtml = htmlMessage != null;

        if (isHtml) {
            HtmlEmail email = new HtmlEmail();

            prepareEmail(to, subject, email);

            email.setHtmlMsg(htmlMessage);
            email.setTextMsg(message);
            return email.send();
        } else {
            SimpleEmail email = new SimpleEmail();

            prepareEmail(to, subject, email);

            email.setMsg(message);
            if (listUnsubscribe != null)
                email.addHeader("List-Unsubscribe", "<" + listUnsubscribe + ">");
            return email.send();
        }
    }
}
