package controllers;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import play.Configuration;
import play.Logger;
import play.Play;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 05.01.13
 * Time: 22:12
 */
public class Email {

    public static String sendEmailTest(String to, String subject, String message) throws EmailException {
        Configuration cfg = Play.application().configuration().getConfig("mail");

        SimpleEmail email = new SimpleEmail();
        email.setHostName(cfg.getString("host"));
        email.setSmtpPort(cfg.getInt("port"));

        Boolean needSSL = cfg.getBoolean("ssl");
        if (needSSL != null && needSSL)
            email.setSSL(true);

        Boolean needTLS = cfg.getBoolean("tls");
        if (needTLS != null && needSSL)
            email.setTLS(true);

        String login = cfg.getString("login");
        String password = cfg.getString("password");
        if (login != null && password != null)
            email.setAuthentication(login, password);
        else if (login == null ^ password == null)
            throw new IllegalArgumentException("Need to specify both login and password to send emails");

        email.addTo(to);

        String from = cfg.getString("from");
        String fromName = cfg.getString("from_name");
        if (fromName == null)
            email.setFrom(from);
        else
            email.setFrom(from, fromName);

        email.setSubject(subject);
        email.setMsg(message);

        return email.send();
    }

}
