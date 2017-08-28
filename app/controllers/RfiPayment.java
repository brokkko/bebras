package controllers;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import plugins.applications.RfiResponseForm;

public class RfiPayment extends Controller {

    public static Result error() {
        RfiResponseForm form = getRfiResponseForm();

        form.getApplication().setState(AppliationState.CONFIRMED);
        form.getApps().mayRemoveApplication()

        return ok();
    }

    public static Result success() {
        RfiResponseForm form = getRfiResponseForm();

        return ok();
    }

    public static Result output() {
        RfiResponseForm form = getRfiResponseForm();

        return ok();
    }

    private static RfiResponseForm getRfiResponseForm() {
        RfiResponseForm form = Form.form(RfiResponseForm.class).bindFromRequest().get();
        form.parseOrderId();
        form.checkSignature();
        return form;
    }

}
