package controllers;

import controllers.actions.DcesController;
import models.Event;
import models.Mailer;
import org.apache.commons.mail.EmailException;
import play.i18n.Messages;
import play.mvc.Http;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 05.01.13
 * Time: 22:12
 */
@DcesController
public class Email {

    public static void sendRegistrationConfirmationEmail(Mailer mailer, String greeting, String email, String login, String password, String confirmationUUID) throws EmailException {
        if (greeting != null && !greeting.isEmpty())
            greeting = ", " + greeting;
        else
            greeting = "";

        String registrationLink = routes.Registration.confirmRegistration(Event.currentId(), confirmationUUID, false)
                                          .absoluteURL(Http.Context.current().request());
        String title = Event.current().getTitle();
        mailer.sendEmail(
                email,
                Messages.get("mail.registration.subject", title),
                createLineBreaks(Messages.get("mail.registration.body", greeting, title, registrationLink, login, password))
        );
    }

    public static void sendPasswordRestoreEmail(Mailer mailer, String greeting, String email, String login, String password, String confirmationUUID) throws EmailException {
        if (greeting != null && !greeting.isEmpty())
            greeting = ", " + greeting;
        else
            greeting = "";

        String registrationLink = routes.Registration.confirmRegistration(Event.currentId(), confirmationUUID, true)
                                          .absoluteURL(Http.Context.current().request());
        String title = Event.current().getTitle();
        mailer.sendEmail(
                email,
                Messages.get("mail.password_remind.subject", title),
                createLineBreaks(Messages.get("mail.password_remind.body", greeting, title, registrationLink, login, password))
        );
    }

    private static String createLineBreaks(String line) {
        return line.replaceAll("\\\\n", "\n");
    }
}
