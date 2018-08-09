package plugins.upload;

import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;

import java.io.File;

public class FileDescription implements SerializableUpdatable {
    private String id;
    private String title;

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("id", id);
        serializer.write("title", title);
    }

    @Override
    public void update(Deserializer deserializer) {
        id = deserializer.readString("id");
        title = deserializer.readString("title");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public File getFolderWithFile(Event event, User user) {
        File uploadedFolder = new File(event.getEventDataFolder(), id);
        return new File(uploadedFolder, user.getId().toHexString());
    }
}
