package plugins;

import models.Event;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.08.13
 * Time: 15:55
 */
public class FieldsUploader extends Plugin { //TODO remove accurately,

    @Override
    public void initPage() {
        //do nothing
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public Result doGet(String action, String params) {
        return Controller.TODO;
    }

    @Override
    public Result doPost(String action, String params) {
        return Controller.TODO;
    }


}
