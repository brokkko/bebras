package controllers;

import controllers.actions.DcesController;
import models.applications.Application;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import plugins.applications.RfiResponseForm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@DcesController()
public class RfiPayment extends Controller {

    public static Result output() {
        RfiResponseForm form;
        try {
            form = getRfiResponseForm();
        } catch (IllegalArgumentException e) {
            log("impossible to process the request: " + e.getMessage());
            return ok("impossible to process the request: " + e.getMessage());
        }

        models.applications.Application application = form.getApplication();
        if (application == null) {
            log("unknown application " + form.getApplicationName());
            return ok("unknown application, will not proceed");
        }

        form.getApps().doPayment(
                form.getUser(),
                form.getApplication(),
                SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date())
        );

        if (application.getState() == Application.CONFIRMED) {
            log("application already confirmed " + form.getApplicationName());
            return ok("application already confirmed");
        }

        String confirmationResult = form.getApps().confirmApplication(
                form.getEvent(),
                form.getUser(),
                null,
                application
        );
        if (confirmationResult != null) {
            log("error during confirmation: " + confirmationResult);
            return ok("error during confirmation");
        }

        return ok();
    }

    public static Result success() {
        RfiResponseForm form = getRfiResponseForm();
        flash("page-info", "Оплата заявки " + form.getApplicationName() + " прошла успешно");

        return redirect(form.getApps().getAppsCall());
    }

    public static Result error() {
        RfiResponseForm form = getRfiResponseForm();
        flash("page-info", "Оплата заявки " + form.getApplicationName() + " не удалась");

        return redirect(form.getApps().getAppsCall());
    }

    private static RfiResponseForm getRfiResponseForm() {
        RfiResponseForm form = Form.form(RfiResponseForm.class).bindFromRequest().get();
        form.parseOrderId();
        form.checkSignature();
        return form;
    }

    private static void log(String message) {
        Logger.info("RFI PAYMENT: " + message + ": " + request().body().asText());
    }

}
