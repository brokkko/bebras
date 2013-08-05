package models.results;

import models.newserialization.Deserializer;

/**
 * Created by ilya
 */
public class SettingsInfo extends Info {

    public static SettingsInfo deserialize(Deserializer deserializer) {
        SettingsInfo info = new SettingsInfo();

        for (String field : deserializer.fields()) {
            Object value = deserializer.readString(field);
            if (value == null)
                value = deserializer.readInt(field);
            info.put(field, value);
        }

        return info;
    }

}