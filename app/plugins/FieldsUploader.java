package plugins;

import models.User;
import play.Logger;
import play.mvc.Result;
import play.mvc.Results;
import views.Menu;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.08.13
 * Time: 15:55
 */
public class FieldsUploader extends Plugin {

    @Override
    public void init() {
        if (User.currentRole().hasRight("event admin"))
            Menu.current().addMenuItem("Загрузить данные", getCall());
    }

    @Override
    public Result doGet(String action) {
        if (User.currentRole().hasRight("event admin"))
            return Results.ok(views.html.upload_fields.render());
        return Results.forbidden();
    }

    @Override
    public Result doPost(String action) {
        return Results.notFound();
    }
}
