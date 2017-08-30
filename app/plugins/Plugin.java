package plugins;

import controllers.routes;
import models.Event;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.08.13
 * Time: 14:29
 */
public abstract class Plugin implements SerializableUpdatable {

    private String ref;

    public String getRef() {
        return ref;
    }

    /**
     * Called before all actions are processed
     */
    public abstract void initPage();

    /**
     * Called after event was deserialized
     */
    public abstract void initEvent(Event event);

    public F.Promise<Result> doGet(String action, String params) {
        return F.Promise.pure(Results.notFound());
    }

    public F.Promise<Result> doPost(String action, String params) {
        return F.Promise.pure(Results.notFound());
    }

    public Call getCall() {
        return getCall("go");
    }

    public Call getCall(String action) {
        return getCall(action, true, "");
    }

    public Call getCall(String action, boolean get, String params) {
        return getCall(action, get, params, Event.current());
    }

    public Call getCall(String action, boolean get, String params, Event event) {
        String currentId = event.getId();

        if (params == null)
            params = "";

        if (!params.isEmpty())
            params = params + '/';

        return get ? routes.Plugins.doGet(currentId, ref, action, params) : routes.Plugins.doPost(currentId, ref, action, params); //TODO add params
    }

    //intended to be overridden
    public boolean needsAuthorization() {
        return false;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("ref", ref);
    }

    @Override
    public void update(Deserializer deserializer) {
        ref = deserializer.readString("ref");
    }
}
