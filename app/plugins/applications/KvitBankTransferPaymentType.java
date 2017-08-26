package plugins.applications;

import models.User;
import models.applications.Application;
import models.applications.Kvit;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.io.File;

public class KvitBankTransferPaymentType extends PaymentType {
    //TODO move default kvit here as a parameter from settings


    @Override
    public F.Promise<Result> processGetRequest(Applications apps, String action, String params, boolean level1rights, boolean level2rights) {
        //simple action without authorization
        if ("pdfkvit".equals(action) && "example".equals(params))
            return showPdfKvit(apps, params);
        if ("kvit_example".equals(action))
            return F.Promise.pure(showKvit(apps));

        switch (action) {
            case "kvit":
                if (!level1rights)
                    return F.Promise.pure(Results.forbidden());
                return F.Promise.pure(showKvit(apps, params));

            case "pdfkvit":
                if (!level1rights)
                    return F.Promise.pure(Results.forbidden());
                return showPdfKvit(apps, params);
        }

        return null;
    }

    @Override
    public void serialize(Serializer serializer) {

    }

    @Override
    public void update(Deserializer deserializer) {

    }

    private Result showKvit(Applications apps) {
        User user = User.current();
        Kvit kvit = Kvit.getKvitForUser(user);

        if (kvit.isGenerated())
            return Results.ok(views.html.applications.kvit.render(null, apps, kvit));
        else
            return Results.redirect(controllers.routes.Resources.returnFile(kvit.getKvitFileName()));
    }

    private Result showKvit(Applications apps, String name) {
        User user = User.current();

        Kvit kvit = Kvit.getKvitForUser(user);
        Application application = apps.getApplicationByName(name);
        if (application == null)
            return Controller.notFound();
        return Controller.ok(views.html.applications.kvit.render(application, apps, kvit));
    }

    private F.Promise<Result> showPdfKvit(Applications apps, String name) {
        //https://code.google.com/p/wkhtmltopdf
        //may need to install ubuntu fontconfig package

        final Application application = "example".equals(name) ?
                getExampleApplication(apps) :
                apps.getApplicationByName(name);
        final Kvit kvit = Kvit.getKvitForUser(User.current());

        if (application == null)
            return F.Promise.pure(Controller.notFound());

        F.Promise<File> promiseOfVoid = F.Promise.promise(
                () -> kvit.generatePdfKvit(apps, application)
        );

        return promiseOfVoid.map(
                file -> {
                    Controller.response().setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
                    return Controller.ok(file).as("application/pdf");
                }
        );
    }

    private Application getExampleApplication(Applications apps) {
        return new Application(User.current(), 100, 1, apps.getApplicationTypes().get(0).getTypeName(), Application.NEW);
    }
}
