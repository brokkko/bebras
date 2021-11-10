package plugins.applications;

import models.User;
import models.applications.Application;
import models.newserialization.SerializableUpdatable;
import play.libs.F;
import play.mvc.Result;
import play.twirl.api.Html;

public abstract class PaymentType implements SerializableUpdatable {

    //returns processed or not
    public F.Promise<Result> processGetRequest(Applications apps, String action, String params, boolean level1rights, boolean level2rights) {
        return null;
    }

    //returns processed or not
    public F.Promise<Result> processPostRequest(Applications apps, String action, String params, boolean level1rights, boolean level2rights) {
        return null;
    }

    public abstract Html render(User applicationUser, User payingUser, Applications apps, Application application);
}
