package plugins.certificates.bebras;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

public class BabiorCertificateFactory extends DiplomaFactory {

    private int year;

    public int getYear() {
        return year;
    }

    @Override
    public Diploma getDiploma(User user) {
        return new BabiorCertificate(user, this);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("year", year);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        year = deserializer.readInt("year", 2015);
    }
}
