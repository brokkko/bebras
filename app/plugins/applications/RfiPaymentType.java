package plugins.applications;

import models.User;
import models.applications.Application;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.twirl.api.Html;

public class RfiPaymentType extends PaymentType {
    @Override
    public void serialize(Serializer serializer) {

    }

    @Override
    public void update(Deserializer deserializer) {

    }

    @Override
    public Html render(User user, Applications apps, Application application) {
        return null;
    }

    @Override
    public Html renderPayed(User user, Applications apps, Application application) {
        return null;
    }
}
