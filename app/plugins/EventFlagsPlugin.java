package plugins;

import models.Event;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import play.mvc.Result;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 07.10.13
 * Time: 20:21
 */
public class EventFlagsPlugin extends Plugin {

    private List<String> flags;

    @Override
    public void initPage() {
        //do nothing
    }

    @Override
    public void initEvent(Event event) {
        if (flags == null)
            return;


        for (String flag : flags) {
            int eqPos = flag.indexOf('=');
            if (eqPos < 0)
                event.setExtraField(flag, true);
            else
                event.setExtraField(flag.substring(0, eqPos), flag.substring(eqPos + 1));
        }
    }

    @Override
    public Result doGet(String action, String params) {
        return null;
    }

    @Override
    public Result doPost(String action, String params) {
        return null;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        SerializationTypesRegistry.list(String.class).write(serializer, "flags", flags);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        flags = SerializationTypesRegistry.list(String.class).read(deserializer, "flags");
    }
}
