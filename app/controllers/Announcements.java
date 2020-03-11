package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Announcement;
import models.Event;
import models.UserRole;
import models.forms.RawForm;
import org.apache.commons.mail.EmailException;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.announcements;
import views.html.send_announcement;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 28.10.13
 * Time: 15:01
 */
@LoadEvent
@Authenticated(admin = true)
@DcesController
public class Announcements extends Controller {

    public static Result prepareAnnouncement(String eventId) {
        return ok(announcements.render(new RawForm(), getRoles()));
    }

    public static Result doPrepareAnnouncement(String eventId) {
        RawForm form = new RawForm();
        form.bindFromRequest();

        String role = form.get("role");
        String subject = form.get("subject");
        String message = form.get("message");

        Event event = Event.current();
        UserRole userRole = event.getRole(role);

        if (userRole == UserRole.EMPTY || role == null || role.isEmpty() || subject == null || subject.isEmpty() || message == null || message.isEmpty()) {
            flash("announcement-prepare", "Заполните все поля формы!");
            return ok(announcements.render(form, getRoles()));
        }

        Announcement announcement = new Announcement(subject, message, userRole, event);
        announcement.store();

        try {
            announcement.sendTestMail();
        } catch (EmailException e) {
            flash("announcement-prepare", "Не удалось отослать проверочный email!");
            return ok(announcements.render(form, getRoles()));
        }

        flash("announcement-prepare", "Вам выслано сообщение для проверки правильности");

        return redirect(routes.Announcements.prepareAnnouncement(eventId));
    }

    public static Result fixAnnouncement(String eventId, String announcementId) {
        Announcement announcement = Announcement.getInstance(announcementId);
        if (announcement == null)
            return notFound();

        RawForm form = new RawForm();
        form.put("role", announcement.getRole().getName());
        form.put("subject", announcement.getSubject());
        form.put("message", announcement.getMessage());

        return ok(announcements.render(form, getRoles()));
    }

    public static Result sendAnnouncement(String eventId, String announcementId) {
        Announcement announcement = Announcement.getInstance(announcementId);
        if (announcement == null)
            return notFound();

        return ok(send_announcement.render(announcement));
    }

    private static List<UserRole> getRoles() {
        List<UserRole> roles = new ArrayList<>();

        Event event = Event.current();
        for (UserRole role : event.getRoles())
            if (role.getUsersForm().getField("email") != null) //select only roles that have email
                roles.add(role);
        return roles;
    }

    public static Result doSendAnnouncement(String eventId, String announcementId) {
        Announcement announcement = Announcement.getInstance(announcementId);
        if (announcement == null)
            return notFound();

        if (announcement.isSent())
            return forbidden();

        announcement.setSent(true);
        announcement.store();
        announcement.sendEmails();
        flash("announcement-send", "Сообщения поставлены в очередь на отсылку");

        return redirect(routes.Announcements.sendAnnouncement(eventId, announcementId));
    }
}
