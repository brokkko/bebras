package plugins.certificates;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;

import java.util.List;

public abstract class DiplomaFactory implements SerializableUpdatable {

    private String contestId;
    private List<String> contestIds;

    public abstract Diploma getDiploma(User user);

    public String getContestId() {
        return contestId;
    }

    public List<String> getContestIds() {
        return contestIds;
    }

    @Override
    public void serialize(Serializer serializer) {
        if (contestId != null)
            serializer.write("contest", contestId);
        if (contestIds != null && !contestIds.isEmpty())
            SerializationTypesRegistry.list(String.class).write(serializer, "contests", contestIds);
    }

    @Override
    public void update(Deserializer deserializer) {
        contestId = deserializer.readString("contest");
        contestIds = SerializationTypesRegistry.list(String.class).read(deserializer, "contests");
    }
}
