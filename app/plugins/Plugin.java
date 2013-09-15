package plugins;

import models.Event;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;
import play.mvc.Call;
import play.mvc.Result;

import controllers.routes;

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

    public abstract Result doGet(String action, String params);

    public abstract Result doPost(String action, String params);

    protected Call getCall() {
        return getCall("go");
    }

    protected Call getCall(String action) {
        return getCall(action, true, "");
    }

    protected Call getCall(String action, boolean get, String params) {
        String currentId = Event.currentId();

        if (params != null && !params.isEmpty())
            params = params + '/';

        return get ? routes.Plugins.doGet(currentId, ref, action, params) : routes.Plugins.doPost(currentId, ref, action, params); //TODO add params
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
