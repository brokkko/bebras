package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.*;
import models.forms.RawForm;
import models.newserialization.FormDeserializer;
import models.newserialization.FormSerializer;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.domains;

@DcesController
@Authenticated(admin = true)
@LoadEvent
public class Domains extends Controller {

    public static Result domainInfo(String eventId, String domainName) {
        Domain domain = getDomain(domainName);

        FormSerializer domainFormSerializer = new FormSerializer(Domain.DOMAIN_CHANGE_FORM);
        domain.serialize(domainFormSerializer);

        FormSerializer mailerFormSerializer = new FormSerializer(Mailer.MAILER_CHANGE_FORM);
        domain.getMailer().serialize(mailerFormSerializer);

        return ok(domains.render(domain, domainFormSerializer.getRawForm(), mailerFormSerializer.getRawForm()));
    }

    public static Result doChangeDomainInfo(String eventId, String domainName) {
        Domain domain = getDomain(domainName);

        FormDeserializer deserializer = new FormDeserializer(Domain.DOMAIN_CHANGE_FORM);
        RawForm rawForm = deserializer.getRawForm();

        if (rawForm.hasErrors())
            return ok(domains.render(domain, rawForm, new RawForm()));

        domain.update(deserializer, true);
        domain.store();

        return redirect(routes.Domains.domainInfo(eventId, domain.getName()));
    }

    public static Result doChangeMailerInfo(String eventId, String domainName) {
        Domain domain = getDomain(domainName);

        FormDeserializer deserializer = new FormDeserializer(Mailer.MAILER_CHANGE_FORM);
        RawForm rawForm = deserializer.getRawForm();

        if (rawForm.hasErrors())
            return ok(domains.render(domain, new RawForm(), rawForm));

        domain.getMailer().update(deserializer);
        domain.store();

        return redirect(routes.Domains.domainInfo(eventId, domain.getName()));
    }

    private static Domain getDomain(String domainName) {
        if (domainName == null || domainName.isEmpty())
            return ServerConfiguration.getInstance().getCurrentDomain();
        else
            return Domain.getInstance(domainName);
    }
}