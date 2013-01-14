package controllers;

import models.*;
import models.forms.InputForm;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.login;
import views.html.register;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.01.13
 * Time: 16:39
 */
public class Registration extends Controller {

    public static Result registration(Event event) {
        //TODO use form.errorsAsJson() to ajax validate form

        Event.setCurrent(event);

        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        return ok(register.render(form));
        //TODO it is (almost) impossible to set breakpoint if there is a link to template
    }

    public static Result doRegistration(Event event) {
        Event.setCurrent(event);

        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        MongoObject user = new MongoObject(MongoConnection.COLLECTION_NAME_USERS);
        event.getUsersForm().getObject(user, form);

        if (form.hasErrors())
            return ok(register.render(form));

        String registrationUUID = UUID.randomUUID().toString();
        String confirmationUUID = UUID.randomUUID().toString();
        String password = User.generatePassword();

        String email = user.getString(User.FIELD_EMAIL);
        String login = user.getString(User.FIELD_LOGIN);
        String name = user.getString("name");
        String patronymic = user.getString("patronymic");

        try {
            Email.sendRegistrationConfirmationEmail(name, patronymic, email, login, password, confirmationUUID);
        } catch (EmailException e) {
            Logger.error("Failed to send email", e);
            return internalServerError("Failed to send email");
        }

        user.put(User.FIELD_REGISTRATION_UUID, registrationUUID);
        user.put(User.FIELD_CONFIRMATION_UUID, confirmationUUID);
        user.put(User.FIELD_EVENT, event.getId());
        user.put(User.FIELD_PASS_HASH, User.passwordHash(password));

        user.store();

        return redirect(routes.Registration.waitForEmail(Event.current(), registrationUUID));
    }

    public static Result waitForEmail(Event event, String uuid) {
        Event.setCurrent(event);

        User user = User.getInstance(User.FIELD_REGISTRATION_UUID, uuid);

        if (user == null)
            return badRequest("no such registration");

        String email = user.getEmail();

        return ok(views.html.wait_for_email.render(email));
    }

    public static Result confirmRegistration(Event event, String uuid) {
        Event.setCurrent(event);

        User user = User.getInstance(User.FIELD_CONFIRMATION_UUID, uuid);

        if (user == null)
            return ok(login.render(new DynamicForm(), "page.registration.already_confirmed"));

        user.getStoredObject().put(User.FIELD_REGISTRATION_UUID, null);
        user.getStoredObject().put(User.FIELD_CONFIRMATION_UUID, null);

        user.storedObject.store();

        DynamicForm emptyForm = new DynamicForm();
        return ok(login.render(emptyForm, "page.registration.confirmed"));
    }

    public static Result login(Event event) {
        Event.setCurrent(event);

        DynamicForm emptyForm = new DynamicForm();
        return ok(login.render(emptyForm, null));
    }

    public static Result doLogin(Event event) {
        Event.setCurrent(event);

        DynamicForm form = new DynamicForm();
        form = form.bindFromRequest();

        InputForm loginForm = Forms.getLoginForm();
        StoredObject credentials = new MemoryStoredObject();
        loginForm.getObject(credentials, form);

        if (form.hasErrors())
            return ok(login.render(form, null));

        return null;
    }

    public static Result passwordRemind(Event event) {
        Event.setCurrent(event);


    }
}