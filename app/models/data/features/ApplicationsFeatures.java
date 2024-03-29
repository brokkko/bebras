package models.data.features;

import models.applications.Application;
import models.applications.ApplicationWithUser;
import models.data.FeaturesContestType;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.data.WrappedFeatureValue;
import models.utils.Utils;
import play.twirl.api.Html;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.09.13
 * Time: 11:25
 */
public class ApplicationsFeatures implements FeaturesSet<ApplicationWithUser> {

    private ApplicationWithUser applicationWithUser;

    @Override
    public void load(ApplicationWithUser applicationWithUser) throws Exception {
        this.applicationWithUser = applicationWithUser;
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) throws Exception {
        Application application = applicationWithUser.getApplication();
        String userId = applicationWithUser.getUserId().toString();
        String applicationName = application.getName();
        int state = application.getState();

        switch (featureName) {
            case "confirm":
                if (context.getType() != FeaturesContestType.INTERFACE)
                    return "-";

                if (state == Application.NEW)
                    return new WrappedFeatureValue("-", Html.apply("-"));

                //TODO allow plugins provide tables and thus remove hardcoded "apps" as a ref to plugin
                String pluginRef = applicationWithUser.getRole().equals("SELF_PARTICIPANT") ? "self_apps" : "apps";

                return new WrappedFeatureValue("-", views.html.htmlfeatures.action.render(
                        "confirm-" + userId + "-" + applicationName,
                        state == Application.CONFIRMED ? "Отменить подтверждение заявки " + applicationName : "Подтвердить заявку " + applicationName,
                        controllers.routes.Plugins.doPost(context.getEvent().getId(), pluginRef, "confirm_app", userId + "/" + applicationName + "/"),
                        context.getCurrentCall(),
                        state == Application.CONFIRMED ? "отменить подтверждение" : "подтвердить"
                ));
            case "id":
                return userId;
            case "login":
                String login = applicationWithUser.getLogin();
                if (context.getType() != FeaturesContestType.INTERFACE)
                    return login;
                return new WrappedFeatureValue(
                        login,
                        views.html.htmlfeatures.user_link.render(userId, context.getEvent().getId(), login)
                );
            case "name":
                return applicationName;
            case "size":
                return "" + application.getSize();
            case "created":
                Date created = application.getCreated();
                return created == null ? "недоступно" : Utils.formatObjectCreationTime(created);
            case "state":
                if (state == Application.NEW)
                    return "новая";
                else if (state == Application.PAYED)
                    return "оплачено";
                else
                    return "подтверждено";
            case "comment":
                return application.getComment();
            case "type":
                return application.getType();
        }

        return "";
    }

    @Override
    public void close() throws Exception {
        if (applicationWithUser != null)
            applicationWithUser = null;
    }
}
