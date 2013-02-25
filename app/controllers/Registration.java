package controllers;

import controllers.actions.LoadEvent;
import models.*;
import models.forms.InputForm;
import models.forms.validators.AuthenticatorValidator;
import models.store.MongoObject;
import models.store.StoredObject;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.data.DynamicForm;
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

        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        return ok(register.render(form));
        //TODO it is (almost) impossible to set breakpoint if there is a link to template
    }

    public static Result doRegistration(String eventId) {
        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();
        InputForm registrationForm = Event.current().getUsersForm();

        InputForm.FilledInputForm filledForm = registrationForm.validate(form);

        if (form.hasErrors())
            return ok(register.render(form));

        MongoObject user = new MongoObject(MongoConnection.COLLECTION_NAME_USERS);
        filledForm.fillObject(user);

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

        boolean isEmail = ! passwordRecovery || user.getBoolean(User.FIELD_RESTORE_FOR_EMAIL, true);

        return ok(wait_for_email.render(
                isEmail, isEmail ? user.getEmail() : user.getLogin()
        ));
    }

    public static Result confirmRegistration(String eventId, String uuid, boolean passwordRecovery) {
        User user = User.getInstance(User.FIELD_CONFIRMATION_UUID, uuid);

        if (user == null)
            return ok(login.render(new DynamicForm(), passwordRecovery ? "page.recovery.already_recovered" : "page.registration.already_confirmed"));

        user.put(User.FIELD_REGISTRATION_UUID, null);
        user.put(User.FIELD_CONFIRMATION_UUID, null);
        user.put(User.FIELD_CONFIRMED, true);

        if (passwordRecovery)
            user.put(
                    User.FIELD_PASS_HASH,
                    user.get(User.FIELD_NEW_RECOVERY_PASSWORD)
            );

        user.storedObject.store();

        DynamicForm emptyForm = new DynamicForm();
        return ok(login.render(emptyForm, passwordRecovery ? "page.registration.password_recovered" : "page.registration.confirmed"));
    }

    public static Result login(String eventId) {
        DynamicForm emptyForm = new DynamicForm();
        return ok(login.render(emptyForm, null));
    }

    public static Result doLogin(String eventId) {
        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        InputForm loginForm = Forms.getLoginForm();
        InputForm.FilledInputForm filledForm = loginForm.validate(form);

        if (form.hasErrors())
            return ok(login.render(form, null));

        User user = (User) filledForm.getValidationData(AuthenticatorValidator.VALIDATED_USER);

        session(User.getUsernameSessionKey(), user.getLogin());

        return redirect(routes.UserInfo.contestsList(eventId));
    }

    public static Result passwordRemind(String eventId) {
        return ok(remind.render(new DynamicForm()));
    }

    public static Result doPasswordRemind(String eventId) {
        DynamicForm form = new DynamicForm();

        form = form.bindFromRequest();

        InputForm remindForm = Forms.getPasswordRemindForm();
        InputForm.FilledInputForm filledForm = remindForm.validate(form);

        if (form.hasErrors())
            return ok(remind.render(form));

        //let's understand what is it, email or login

        String emailOrLogin = (String) filledForm.get(Forms.PASSWORD_REMIND_FORM_EMAIL_OR_LOGIN);

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