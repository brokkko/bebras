package plugins.certificates;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;

public abstract class DiplomaFactory implements SerializableUpdatable {

    private String contestId;

    public abstract Diploma getDiploma(User user);

    public String getContestId() {
        return contestId;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("contest", contestId);
    }

    @Override
    public void update(Deserializer deserializer) {
        contestId = deserializer.readString("contest");
    }
}
