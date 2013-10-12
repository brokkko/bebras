package controllers;

import controllers.actions.DcesController;
import models.Event;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import play.Configuration;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.mvc.Http;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 05.01.13
 * Time: 22:12
 */
@DcesController
public class Email {

    public static String sendEmail(String to, String subject, String message) throws EmailException {
        return sendEmail(to, subject, message, null);
    }

    public static String sendEmail(String to, String subject, String message, String htmlMessage) throws EmailException {
        boolean isHtml = htmlMessage == null;

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
            return email.send();
        }
    }

    private static void prepareEmail(String to, String subject, org.apache.commons.mail.Email email) throws EmailException {
        Configuration cfg = Play.application().configuration().getConfig("mail");

        email.setHostName(cfg.getString("host"));
        email.setSmtpPort(cfg.getInt("port"));

        Boolean needSSL = cfg.getBoolean("ssl");
        if (needSSL != null && needSSL)
            email.setSSL(true);

        Boolean needTLS = cfg.getBoolean("tls");
        if (needTLS != null && needTLS)
            email.setTLS(true);

        String login = cfg.getString("login");
        String password = cfg.getString("password");
        if (login != null && password != null)
            email.setAuthentication(login, password);
        else if (login == null ^ password == null)
            throw new IllegalArgumentException("Need to specify both login and password to send emails");

        email.addTo(to);

        String from = Event.currentId().startsWith("bebras") ? "noreply@bebras.ru" : cfg.getString("from"); //TODO get rid of bebras
        String fromName = Event.currentId().startsWith("bebras") ? "Bebras contest" : cfg.getString("from_name"); //TODO get rid of bebras
        if (fromName == null)
            email.setFrom(from);
        else
            email.setFrom(from, fromName);

        String replyTo = Event.currentId().startsWith("bebras") ? "org@bebras.ru" : cfg.getString("reply_to"); //TODO get rid of bebras

        if (replyTo != null)
            try {
                email.setReplyTo(Arrays.asList(new InternetAddress(replyTo)));
            } catch (AddressException e) {
                Logger.error("Failed to make Internet address out of " + replyTo);
            }

        email.setCharset("UTF8");
        email.setSubject(subject);
    }

    public static void sendRegistrationConfirmationEmail(String greeting, String email, String login, String password, String confirmationUUID) throws EmailException {
        if (greeting != null && !greeting.isEmpty())
            greeting = ", " + greeting;
        else
            greeting = "";

        String registrationLink = routes.Registration.confirmRegistration(Event.currentId(), confirmationUUID, false)
                                          .absoluteURL(Http.Context.current().request());
        String title = Event.current().getTitle();
        sendEmail(
                         email,
                         Messages.get("mail.registration.subject", title),
                         createLineBreaks(Messages.get("mail.registration.body", greeting, title, registrationLink, login, password))
        );
    }

    public static void sendPasswordRestoreEmail(String greeting, String email, String login, String password, String confirmationUUID) throws EmailException {
        if (greeting != null && !greeting.isEmpty())
            greeting = ", " + greeting;
        else
            greeting = "";

        String registrationLink = routes.Registration.confirmRegistration(Event.currentId(), confirmationUUID, true)
                                          .absoluteURL(Http.Context.current().request());
        String title = Event.current().getTitle();
        sendEmail(
                         email,
                         Messages.get("mail.password_remind.subject", title),
                         createLineBreaks(Messages.get("mail.password_remind.body", greeting, title, registrationLink, login, password))
        );
    }

    private static String createLineBreaks(String line) {
        return line.replaceAll("\\\\n", "\n");
    }
}
