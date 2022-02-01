package controllers;

import controllers.actions.DcesController;
import models.applications.Application;
import models.forms.RawForm;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import plugins.applications.Applications;
import plugins.applications.PaymentType;
import plugins.applications.RfiPaymentType;
import plugins.applications.RfiResponseForm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static plugins.applications.RfiResponseForm.CAN_NOT_CHECK_SIGNATURE;

@DcesController()
public class RfiPayment extends Controller {

    public static Result output() {
        RfiResponseForm form = getRfiResponseForm();
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

        //search for RFI payment type
        final Applications apps = form.getApps();
        if (apps == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGNATURE);
        List<PaymentType> paymentTypes = apps.getPaymentTypes();
        RfiPaymentType pay = null;
        for (PaymentType paymentType : paymentTypes)
            if (paymentType instanceof RfiPaymentType) {
                pay = (RfiPaymentType) paymentType;
                break;
            }
        if (pay == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGNATURE);

        if (isOutputRequest) {
            form.checkOutputSignature(pay);
            form.checkTransactionCorrect();
        } else
            form.checkFailSuccessSignature(pay);

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
