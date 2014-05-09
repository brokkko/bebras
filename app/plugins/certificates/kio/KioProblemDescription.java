package plugins.certificates.kio;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;

import java.util.List;

public class KioProblemDescription implements SerializableUpdatable {

    private String name;
    private String pattern;
    private String rankField;
    private List<String> fields;

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public String getRankField() {
        return rankField;
    }

    public String getScoresField() {
        return rankField.replace("rank_", "scores_");
    }

    public List<String> getFields() {
        return fields;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        serializer.write("pattern", pattern);
        serializer.write("scores", rankField); //TODO rename scores to rank
        SerializationTypesRegistry.list(String.class).write(serializer, "fields", fields);
    }

    @Override
    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        pattern = deserializer.readString("pattern");
        rankField = deserializer.readString("scores");
        fields = SerializationTypesRegistry.list(String.class).read(deserializer, "fields");
    }
}
