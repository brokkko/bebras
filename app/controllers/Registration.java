package controllers;

import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.*;
import models.forms.*;
import models.forms.validators.AuthenticatorValidator;
import models.newserialization.FormDeserializer;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.01.13
 * Time: 16:39
 */
@LoadEvent
@DcesController
public class Registration extends Controller {

    @SuppressWarnings("UnusedParameters")
    public static Result registration(String eventId) {
        //TODO use form.errorsAsJson() to ajax validate form

        if (!Event.current().registrationStarted())
            return ok(views.html.error.render("error.msg.registration_not_started", new String[0]));

        if (Event.current().registrationFinished())
            return ok(views.html.error.render("error.msg.registration_finished", new String[0]));

        RawForm form = new RawForm();
        form.bindFromRequest();

        return ok(views.html.register.render(form));
    }

    public static Result doRegistration(String eventId) {
        InputForm registrationForm = Event.current().getUsersForm();

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm);
        RawForm rawForm = formDeserializer.getRawForm();

        if (rawForm.hasErrors())
            return ok(views.html.register.render(rawForm));

        User user = User.deserialize(formDeserializer);

        String registrationUUID = UUID.randomUUID().toString();
        String confirmationUUID = UUID.randomUUID().toString();
        String password = User.generatePassword();

        String email = user.getEmail();
        String login = user.getLogin();
        String greeting = user.getGreeting();

        try {
            Email.sendRegistrationConfirmationEmail(greeting, email, login, password, confirmationUUID);
        } catch (EmailException e) {
            Logger.error("Failed to send email", e);
            if (!Play.isDev()) //allow to fail when sending email in dev mode
                return internalServerError("Failed to send email");
        }

        user.setRegistrationUUID(registrationUUID);
        user.setConfirmationUUID(confirmationUUID);
        user.setEvent(Event.current());
        user.setPasswordHash(User.passwordHash(password));

        user.store();

        return redirect(routes.Registration.waitForEmail(eventId, registrationUUID, false));
    }

    public static Result waitForEmail(String eventId, String uuid, boolean passwordRecovery) {
        User user = User.getUserByRegistrationUUID(uuid);

        if (user == null)
            return redirect(routes.Registration.login(eventId));

        Boolean restoreForEmail = user.isRestoreForEmail();
        boolean isEmail = ! passwordRecovery || restoreForEmail == null || restoreForEmail;

        return ok(views.html.wait_for_email.render(
                isEmail, isEmail ? user.getEmail() : user.getLogin()
        ));
    }

    @SuppressWarnings("UnusedParameters")
    public static Result confirmRegistration(String eventId, String uuid, boolean passwordRecovery) {
        User user = User.getUserByConfirmationUUID(uuid);

        //allow in dev mode to confirm by login
        if (user == null && Play.isDev())
            user = User.getUserByLogin(uuid);

        if (user == null)
            return ok(views.html.login.render(new RawForm(), passwordRecovery ? "page.recovery.already_recovered" : "page.registration.already_confirmed"));

        user.setRegistrationUUID(null);
        user.setConfirmationUUID(null);
        user.setConfirmed(true);

        if (passwordRecovery)
            user.setPasswordHash(user.getNewRecoveryPassword());

        user.store();

        return ok(views.html.login.render(new RawForm(), passwordRecovery ? "page.registration.password_recovered" : "page.registration.confirmed"));
    }

    @SuppressWarnings("UnusedParameters")
    public static Result login(String eventId) {
        return ok(views.html.login.render(new RawForm(), null));
    }

    public static Result doLogin(String eventId) {
        InputForm loginForm = Forms.getLoginForm();
        FormDeserializer formDeserializer = new FormDeserializer(loginForm);

        RawForm form = formDeserializer.getRawForm();

        if (form.hasErrors())
            return ok(views.html.login.render(form, null));

        User user = (User) formDeserializer.getValidationData();

        session(User.getUsernameSessionKey(), user.getLogin());

        return redirect(routes.UserInfo.contestsList(eventId));
    }

    @SuppressWarnings("UnusedParameters")
    public static Result passwordRemind(String eventId) {
        return ok(views.html.remind.render(new RawForm()));
    }

    public static Result doPasswordRemind(String eventId) {
        InputForm remindForm = Forms.getPasswordRemindForm();
        FormDeserializer formDeserializer = new FormDeserializer(remindForm);
        RawForm form = formDeserializer.getRawForm();

        if (form.hasErrors())
            return ok(views.html.remind.render(form));

        //let's understand what is it, email or login

        String emailOrLogin = formDeserializer.readString(Forms.PASSWORD_REMIND_FORM_EMAIL_OR_LOGIN);

        boolean isEmail = emailOrLogin.contains("@");

        User user = isEmail ? User.getUserByEmail(emailOrLogin) : User.getUserByLogin(emailOrLogin);

        if (user == null) {
            String error_message = isEmail ? "password_remind.wrong_email" : "password_remind.wrong_login";
            form.reject(Messages.get(error_message));
            return ok(views.html.remind.render(form));
        }

        String newPassword = User.generatePassword();

        String recoveryUUID = user.getRegistrationUUID();
        String confirmationUUID = user.getConfirmationUUID();

        if (recoveryUUID == null)
            recoveryUUID = UUID.randomUUID().toString();
        if (confirmationUUID == null)
            confirmationUUID = UUID.randomUUID().toString();

        try {
            Email.sendPasswordRestoreEmail(
                    user.getGreeting(),
                    user.getEmail(),
                    user.getLogin(),
                    newPassword,
                    confirmationUUID
            );
        } catch (EmailException e) {
            Logger.error("Failed to send email", e);
            return internalServerError("Failed to send email");
        }

        user.setConfirmationUUID(confirmationUUID);
        user.setRegistrationUUID(recoveryUUID);
        user.setRestoreForEmail(isEmail);
        user.setNewRecoveryPassword(User.passwordHash(newPassword));

        user.store();

        return redirect(routes.Registration.waitForEmail(eventId, recoveryUUID, true));
    }

    public static Result logout(String eventId) {
        session().clear();

        return redirect(routes.Registration.login(eventId));
    }
}