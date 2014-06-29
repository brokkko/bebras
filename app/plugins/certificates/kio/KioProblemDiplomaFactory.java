package plugins.certificates.kio;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

import java.util.Arrays;
import java.util.List;

public class KioProblemDiplomaFactory extends DiplomaFactory {

    private String diplomaField;
    private String problemRankField;
    private int year;
    private List<String> problemNames;

    @Override
    public Diploma getDiploma(User user) {
        return new KioProblemDiploma(user, this);
    }

    public String getDiplomaField() {
        return diplomaField;
    }

    public String getProblemRankField() {
        return problemRankField;
    }

    public int getYear() {
        return year;
    }

    public String getProblemName(int level) {
        if (problemNames.size() == 1)
            return problemNames.get(0);
        else
            return problemNames.get(level);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("diploma field", diplomaField);
        serializer.write("rank field", problemRankField);
        serializer.write("year", year);

        if (problemNames.size() == 1)
            serializer.write("name", problemNames.get(0));
        else
            SerializationTypesRegistry.list(String.class).write(serializer, "names", problemNames);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        diplomaField = deserializer.readString("diploma field");
        problemRankField = deserializer.readString("rank field");
        year = deserializer.readInt("year", 2014);

        String problemName = deserializer.readString("name");
        if (problemName != null)
            problemNames = Arrays.asList(problemName);
        else
            problemNames = SerializationTypesRegistry.list(String.class).read(deserializer, "names");
    }
}
