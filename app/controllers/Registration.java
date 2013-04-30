package controllers;

import controllers.actions.LoadEvent;
import models.*;
import models.newmodel.*;
import models.newmodel.validators.AuthenticatorValidator;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.01.13
 * Time: 16:39
 */

@LoadEvent
public class Registration extends Controller {

    public static Result registration(String eventId) {
        //TODO use form.errorsAsJson() to ajax validate form

        RawForm form = new RawForm();
        form.bindFromRequest();

        return ok(register.render(form));
    }

    public static Result doRegistration(String eventId) {
        InputForm registrationForm = Event.current().getUsersForm();

        FormDeserializer formDeserializer = new FormDeserializer(registrationForm);
        RawForm rawForm = formDeserializer.getRawForm();

        if (rawForm.hasErrors())
            return ok(register.render(rawForm));

        User user = User.deserialize(formDeserializer);

        String registrationUUID = UUID.randomUUID().toString();
        String confirmationUUID = UUID.randomUUID().toString();
        String password = User.generatePassword();

        String email = user.getString(User.FIELD_EMAIL);
        String login = user.getString(User.FIELD_LOGIN);
        String name = user.getString(User.FIELD_NAME);
        String patronymic = user.getString(User.FIELD_PATRONYMIC);

        try {
            Email.sendRegistrationConfirmationEmail(name, patronymic, email, login, password, confirmationUUID);
        } catch (EmailException e) {
            Logger.error("Failed to send email", e);
            return internalServerError("Failed to send email");
        }

        user.put(User.FIELD_REGISTRATION_UUID, registrationUUID);
        user.put(User.FIELD_CONFIRMATION_UUID, confirmationUUID);
        user.put(User.FIELD_EVENT, eventId);
        user.put(User.FIELD_PASS_HASH, User.passwordHash(password));

        user.store();

        return redirect(routes.Registration.waitForEmail(eventId, registrationUUID, false));
    }

    public static Result waitForEmail(String eventId, String uuid, boolean passwordRecovery) {
        User user = User.getInstance(User.FIELD_REGISTRATION_UUID, uuid);

        if (user == null)
            return redirect(routes.Registration.login(eventId));

        Boolean restoreForEmail = (Boolean) user.get(User.FIELD_RESTORE_FOR_EMAIL);
        boolean isEmail = ! passwordRecovery || restoreForEmail == null || restoreForEmail;

        return ok(wait_for_email.render(
                isEmail, isEmail ? user.getEmail() : user.getLogin()
        ));
    }

    public static Result confirmRegistration(String eventId, String uuid, boolean passwordRecovery) {
        User user = User.getInstance(User.FIELD_CONFIRMATION_UUID, uuid);

        if (user == null)
            return ok(login.render(new RawForm(), passwordRecovery ? "page.recovery.already_recovered" : "page.registration.already_confirmed"));

        user.put(User.FIELD_REGISTRATION_UUID, null);
        user.put(User.FIELD_CONFIRMATION_UUID, null);
        user.put(User.FIELD_CONFIRMED, true);

        if (passwordRecovery)
            user.put(
                    User.FIELD_PASS_HASH,
                    user.get(User.FIELD_NEW_RECOVERY_PASSWORD)
            );

        user.store();

        return ok(login.render(new RawForm(), passwordRecovery ? "page.registration.password_recovered" : "page.registration.confirmed"));
    }

    public static Result login(String eventId) {
        return ok(login.render(new RawForm(), null));
    }

    public static Result doLogin(String eventId) {
        InputForm loginForm = Forms.getLoginForm();
        FormDeserializer formDeserializer = new FormDeserializer(loginForm);

        RawForm form = formDeserializer.getRawForm();

        if (form.hasErrors())
            return ok(login.render(form, null));

        User user = (User) formDeserializer.getValidationData(AuthenticatorValidator.VALIDATED_USER);

        session(User.getUsernameSessionKey(), user.getLogin());

        return redirect(routes.UserInfo.contestsList(eventId));
    }

    public static Result passwordRemind(String eventId) {
        return ok(remind.render(new RawForm()));
    }

    public static Result doPasswordRemind(String eventId) {
        InputForm remindForm = Forms.getPasswordRemindForm();
        FormDeserializer formDeserializer = new FormDeserializer(remindForm);
        RawForm form = formDeserializer.getRawForm();

        if (form.hasErrors())
            return ok(remind.render(form));

        //let's understand what is it, email or login

        String emailOrLogin = formDeserializer.getString(Forms.PASSWORD_REMIND_FORM_EMAIL_OR_LOGIN);

        boolean isEmail = emailOrLogin.contains("@");

        User user = User.getInstance(isEmail ? User.FIELD_EMAIL : User.FIELD_LOGIN, emailOrLogin);

        if (user == null) {
            String error_message = isEmail ? "password_remind.wrong_email" : "password_remind.wrong_login";
            form.reject(Messages.get(error_message));
            return ok(remind.render(form));
        }

        String newPassword = User.generatePassword();

        String recoveryUUID = (String) user.get(User.FIELD_REGISTRATION_UUID);
        String confirmationUUID = (String) user.get(User.FIELD_CONFIRMATION_UUID);

        if (recoveryUUID == null)
            recoveryUUID = UUID.randomUUID().toString();
        if (confirmationUUID == null)
            confirmationUUID = UUID.randomUUID().toString();

        try {
            Email.sendPasswordRestoreEmail(
                    user.getString(User.FIELD_NAME),
                    user.getString(User.FIELD_PATRONYMIC),
                    user.getEmail(),
                    user.getLogin(),
                    newPassword,
                    confirmationUUID
            );
        } catch (EmailException e) {
            Logger.error("Failed to send email", e);
            return internalServerError("Failed to send email");
        }

        user.put(User.FIELD_CONFIRMATION_UUID, confirmationUUID);
        user.put(User.FIELD_REGISTRATION_UUID, recoveryUUID);
        user.put(User.FIELD_RESTORE_FOR_EMAIL, isEmail);
        user.put(User.FIELD_NEW_RECOVERY_PASSWORD, User.passwordHash(newPassword));

        user.store();

        return redirect(routes.Registration.waitForEmail(eventId, recoveryUUID, true));
    }

    public static Result logout(String eventId) {
        session().clear();

        return redirect(routes.Registration.login(eventId));
    }
}