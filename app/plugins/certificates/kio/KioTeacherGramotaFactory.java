package plugins.certificates.kio;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

public class KioTeacherGramotaFactory extends DiplomaFactory {

    private int year;

    @Override
    public Diploma getDiploma(User user) {
        return new KioTeacherGramota(user, this);
    }

    public int getYear() {
        return year;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("year", year);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        year = deserializer.readInt("year");
    }
}
