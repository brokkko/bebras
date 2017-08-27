package plugins.applications;

import models.Event;
import models.User;
import models.applications.Application;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.twirl.api.Html;
import views.html.applications.rfi_payment;

public class RfiPaymentType extends PaymentType {

    private String serviceId;
    private String secretKey;

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("service_id", serviceId);
        serializer.write("secret", secretKey);
    }

    @Override
    public void update(Deserializer deserializer) {
        serviceId = deserializer.readString("service_id", "??");
        secretKey = deserializer.readString("secret", "??");
    }

    @Override
    public Html render(User user, Applications apps, Application application) {
        Event event = Event.current();
        RfiPaymentForm form = new RfiPaymentForm(event, this, apps, application, user);

        return rfi_payment.render(form);
    }

    @Override
    public Html renderPayed(User user, Applications apps, Application application) {
        return null;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
