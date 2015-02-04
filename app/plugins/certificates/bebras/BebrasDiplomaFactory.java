package plugins.certificates.bebras;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

public class BebrasDiplomaFactory extends DiplomaFactory {

    private int year;
    private String userDiplomaLevelField;

    @Override
    public Diploma getDiploma(User user) {
        return new BebrasDiploma(user, this);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("year", year);
        serializer.write("diploma level field", userDiplomaLevelField);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        year = deserializer.readInt("year", 2014);
        userDiplomaLevelField = deserializer.readString("diploma level field", "diploma_level");
    }

    public int getYear() {
        return year;
    }

    public String getUserDiplomaLevelField() {
        return userDiplomaLevelField;
    }
}
