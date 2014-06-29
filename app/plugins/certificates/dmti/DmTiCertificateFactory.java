package plugins.certificates.dmti;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

import java.util.HashMap;
import java.util.Map;

public class DmTiCertificateFactory extends DiplomaFactory {

    @Override
    public Diploma getDiploma(User user) {
        return new DmTiCertificate(user, this);
    }

}
