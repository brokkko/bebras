package plugins.applications;

import models.User;
import models.applications.Application;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.twirl.api.Html;
import views.html.applications.set_comment_button;

public class SelfConfirmPaymentType extends PaymentType {
    @Override
    public void serialize(Serializer serializer) {

    }

    @Override
    public void update(Deserializer deserializer) {

    }

    @Override
    public Html render(User applicationUser, User payingUser, Applications apps, Application application) {
        return set_comment_button.render(apps, application);
    }

    @Override
    public Html renderPayed(User applicationUser, User payingUser, Applications apps, Application application) {
        return Html.apply("");
    }
}
