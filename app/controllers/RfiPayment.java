package controllers;

import controllers.actions.DcesController;
import models.applications.Application;
import models.forms.RawForm;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import plugins.applications.RfiResponseForm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@DcesController()
public class RfiPayment extends Controller {

    public static Result output() {
        RfiResponseForm form = getRfiResponseForm();
        log(form, "start processing RFI output");
        try {
            processForm(form, true);
        } catch (IllegalArgumentException e) {
            log(form,"impossible to process the request: " + e.getMessage());
            return ok("impossible to process the request: " + e.getMessage());
        }

        models.applications.Application application = form.getApplication();
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
        RfiResponseForm form = getRfiResponseForm();
        log(form,"Payment return " + message);
        try {
            processForm(form, false);
        } catch (IllegalArgumentException e) {
            log(form,"RFI bad success request" + e.getMessage());
            return badRequest("failed to parse form");
        }
        flash("page-info", "Оплата заявки " + form.getApplicationName() + " " + message);

        return redirect(form.getApps().getViewAppCall(form.getUser(), form.getApplicationName()));
    }

    private static void processForm(RfiResponseForm form, boolean isOutputRequest) {
        form.serialize();
        IllegalArgumentException parserException = null;
        try {
            form.parseOrderInformation();
        } catch (IllegalArgumentException e) {
            parserException = e;
        }

        if (isOutputRequest)
            form.checkOutputSignature();
        else
            form.checkFailSuccessSignature();

        log(form,"Form signature test ok");

        if (parserException != null)
            throw parserException;
    }

    private static RfiResponseForm getRfiResponseForm() {
        final RawForm rawForm = new RawForm();
        rawForm.bindFromRequest();
        final RfiResponseForm responseForm = Form.form(RfiResponseForm.class).bindFromRequest().get();
        final Http.Request request = request();
        responseForm.updateWithRequestInfo(rawForm, request.method(), request.host(), request.path());

        return responseForm;
    }

    private static void log(RfiResponseForm form, String message) {
        Logger.info("RFI PAYMENT: " + message + ": " + form);
    }

}
