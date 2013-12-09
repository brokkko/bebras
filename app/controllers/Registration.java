package controllers;

import controllers.actions.Authenticated;
import controllers.actions.AuthenticatedAction;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.*;
import models.forms.*;
import models.newserialization.FormDeserializer;
import org.apache.commons.mail.EmailException;
import org.bson.types.ObjectId;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
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
        Event event = Event.current();
        return processSelectRegistration(event, event.getAnonymousRole(), false);
    }

    @SuppressWarnings("UnusedParameters")
    @Authenticated
    public static Result registrationByUser(String eventId) {
        Event event = Event.current();
        return processSelectRegistration(event, User.currentRole(), true);
    }

    private static Result processSelectRegistration(Event event, UserRole registerRole, boolean byUser) {
        List<String> mayRegister = registerRole.getMayRegister();

        if (mayRegister.size() == 0)
            return forbidden();

        if (mayRegister.size() == 1) {
            String roleName = mayRegister.get(0);
            return byUser ?
                    redirect(routes.Registration.roleRegistrationByUser(event.getId(), roleName)) :
                    redirect(routes.Registration.roleRegistration(event.getId(), roleName, null));
        }

        return ok(views.html.registration_select.render(event, registerRole, byUser));
    }

    @SuppressWarnings("UnusedParameters")
    public static Result roleRegistration(String eventId, String roleName, String referrerUserId) {
        Event event = Event.current();
        UserRole registeeRole = event.getRole(roleName);

        if (!Event.current().registrationStarted())
            return ok(views.html.error.render("error.msg.registration_not_started", new String[0]));

        if (Event.current().registrationFinished())
            return ok(views.html.error.render("error.msg.registration_finished", new String[0]));

        if (registeeRole == UserRole.EMPTY)
            return forbidden();

        return processRoleRegistration(event, event.getAnonymousRole(), registeeRole, false, referrerUserId);
    }

    @SuppressWarnings("UnusedParameters")
    @Authenticated
    public static Result roleRegistrationByUser(String eventId, String roleName) {
        Event event = Event.current();
        UserRole registeeRole = event.getRole(roleName);

        if (registeeRole == UserRole.EMPTY)
            return forbidden();

        return processRoleRegistration(event, User.currentRole(), registeeRole, true, null);
    }

    public static Result processRoleRegistration(Event event, UserRole registerRole, UserRole registeeRole, boolean byUser, String referrerUserId) {
        //TODO use form.errorsAsJson() to ajax validate form

        if (!registerRole.mayRegister(registeeRole))
            return forbidden();

        RawForm form = new RawForm();
        form.bindFromRequest();

        return ok(views.html.register.render(form, registeeRole, byUser, referrerUserId));
    }

    @SuppressWarnings("UnusedParameters")
    public static Result doRegistration(String eventId, String roleName, String referrerUserId) {
        Event event = Event.current();
        return processRegistration(event, event.getAnonymousRole(), event.getRole(roleName), false, referrerUserId);
    }

    @SuppressWarnings("UnusedParameters")
    @Authenticated
    public static Result doRegistrationByUser(String eventId, String roleName) {
        Event event = Event.current();
        return processRegistration(event, User.currentRole(), event.getRole(roleName), true, null);
    }

    private static Result processRegistration(Event event, UserRole registerRole, UserRole registeeRole, boolean byUser, String referrerUserId) {
        //test roles
        if (!registerRole.mayRegister(registeeRole))
            return forbidden();

        //read form
        InputForm registrationForm = registeeRole.getUsersForm();

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm);
        RawForm rawForm = formDeserializer.getRawForm();

        boolean isPartialRegistration = false;

        if (rawForm.hasErrors())
            if (!byUser || !formDeserializer.isPartiallyFilled("login", "email")) //TODO think about this hardcoding of login and email as required fields
                return ok(views.html.register.render(rawForm, registeeRole, byUser, referrerUserId));
            else
                isPartialRegistration = true;

        //add value for user role
        formDeserializer.addValue(User.FIELD_USER_ROLE, registeeRole.getName());
        formDeserializer.addValue(User.FIELD_PARTIAL_REG, isPartialRegistration);

        User user = User.deserialize(formDeserializer);

        if (setRegisterBy(registeeRole, byUser, referrerUserId, user))
            return badRequest();

        String email = user.getEmail();
        String login = user.getLogin();
        String greeting = user.getGreeting();

        String registrationUUID = null;
        String confirmationUUID = null;

        String password;

        boolean needEmailConfirmation = formDeserializer.isNull("password");

        if (needEmailConfirmation) {
            registrationUUID = UUID.randomUUID().toString();
            confirmationUUID = UUID.randomUUID().toString();

            password = User.generatePassword();

            try {
                Email.sendRegistrationConfirmationEmail(
                        ServerConfiguration.getInstance().getCurrentDomain().getMailer(),
                        greeting, email, login, password, confirmationUUID
                );
            } catch (EmailException e) {
                Logger.error("Failed to send email", e);
                if (!Play.isDev()) //allow to fail when sending email in dev mode
                    return internalServerError("Failed to send email");
            }
        } else {
            password = formDeserializer.readString("password");
            user.setConfirmed(true);
        }

        user.setRegistrationUUID(registrationUUID);
        user.setConfirmationUUID(confirmationUUID);

        user.setEvent(Event.current());
        user.setPasswordHash(User.passwordHash(password));

        user.store();

        return needEmailConfirmation ?
                redirect(routes.Registration.waitForEmail(event.getId(), registrationUUID, false)) :
                ok(views.html.message.render("registration.ok.title", "registration.ok", null));
    }

    private static boolean setRegisterBy(UserRole registeeRole, boolean byUser, String referrerUserId, User user) {
        ObjectId registerBy = null;
        if (byUser)
            registerBy = User.current().getId();
        if (referrerUserId != null) {
            try {
                registerBy = new ObjectId(referrerUserId);
            } catch (Exception ignored) { //illegal argument exception
                return true;
            }
            User referrer = User.getInstance("_id", registerBy);
            if (referrer == null)
                return true;
            if (!referrer.getRole().mayRegister(registeeRole))
                return true;
        }

        user.setRegisteredBy(registerBy);
        return false;
    }

    public static Result waitForEmail(String eventId, String uuid, boolean passwordRecovery) {
        User user = User.getUserByRegistrationUUID(uuid);

        if (user == null)
            return redirect(routes.Registration.login(eventId));

        Boolean restoreForEmail = user.isRestoreForEmail();
        boolean isEmail = !passwordRecovery || restoreForEmail == null || restoreForEmail;

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

    @Authenticated(redirectToLogin = false)
    @SuppressWarnings("UnusedParameters")
    public static Result login(String eventId) {
        if (User.isAuthorized())
            return redirect(routes.Application.enter(eventId));

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

        return redirect(routes.Application.enter(eventId));
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

        if (user.getEmail() == null) {
            form.reject("password_remind.no_email");
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
                    ServerConfiguration.getInstance().getCurrentDomain().getMailer(),
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