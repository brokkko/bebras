package plugins;

import models.newserialization.SerializableTreeSerializationType;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.08.13
 * Time: 15:55
 */
public class PluginSerializationType extends SerializableTreeSerializationType<Plugin> {

    public PluginSerializationType() {
        registerClass("fields uploader", FieldsUploader.class);
        registerClass("extra page", ExtraPage.class);
        registerClass("applications", Applications.class);
        registerClass("link", LinkPlugin.class);
        registerClass("flags", EventFlagsPlugin.class);
    }

}
