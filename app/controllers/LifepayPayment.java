package controllers;

import controllers.actions.DcesController;
import models.applications.Application;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import plugins.applications.LifepayResponseForm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@DcesController()
public class LifepayPayment extends Controller {

    public static Result output() {
        LifepayResponseForm form = getLifepayResponseForm();
        try {
            processForm(form);
        } catch (IllegalArgumentException e) {
            log(form,"impossible to process the request: " + e.getMessage());
            return ok("impossible to process the request: " + e.getMessage());
        }

        Application application = form.getApplication();
        if (application == null) {
            log(form,"unknown application " + form.getApplicationName());
            return ok("unknown application, will not proceed");
        }

        form.getApps().doPayment(
                form.getUser(),
                form.getApplication(),
                SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date())
        );

        if (application.getState() == Application.CONFIRMED) {
            log(form,"application already confirmed " + form.getApplicationName());
            return ok("application already confirmed");
        }

        String confirmationResult = form.getApps().confirmApplication(
                form.getEvent(),
                form.getUser(),
                null,
                application
        );
        if (confirmationResult != null) {
            log(form,"error during confirmation: " + confirmationResult);
            return ok("error during confirmation");
        }

        return ok();
    }

    public static Result success() {
        return processSuccessOrErrorUserRedirection("прошла успешно");
    }

    public static Result error() {
        return processSuccessOrErrorUserRedirection("не удалась");
    }

    private static Result processSuccessOrErrorUserRedirection(String message) {
        LifepayResponseForm form = getLifepayResponseForm();
        try {
            processForm(form);
        } catch (IllegalArgumentException e) {
            Logger.info("Lifepay bad success request: " + e.getMessage() + " : " + form);
            return badRequest("failed to parse form");
        }
        flash("page-info", "Оплата заявки " + form.getApplicationName() + " " + message);

        return redirect(form.getApps().getViewAppCall(form.getUser(), form.getApplicationName()));
    }

    private static void processForm(LifepayResponseForm form) {
        form.serialize();
        IllegalArgumentException parserException = null;
        try {
            form.parseOrderInformation();
        } catch (IllegalArgumentException e) {
            parserException = e;
        }

        form.checkSignature();

        if (parserException != null)
            throw parserException;
    }

    private static LifepayResponseForm getLifepayResponseForm() {
        return Form.form(LifepayResponseForm.class).bindFromRequest().get();
    }

    private static void log(LifepayResponseForm form, String message) {
        Logger.info("Lifepay PAYMENT: " + message + ": " + form);
    }

}
