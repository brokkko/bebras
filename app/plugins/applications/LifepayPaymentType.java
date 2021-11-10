package plugins.applications;

import models.Event;
import models.User;
import models.applications.Application;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.twirl.api.Html;
import views.html.applications.lifepay_payment;

public class LifepayPaymentType extends PaymentType {

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
    public Html render(User applicationUser, User payingUser, Applications apps, Application application) {
        LifepayPaymentForm form = new LifepayPaymentForm(this, apps, application, payingUser, applicationUser);
        return lifepay_payment.render(form);
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
