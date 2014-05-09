package plugins.certificates;

import models.User;
import models.newserialization.SerializableUpdatable;

public interface DiplomaFactory extends SerializableUpdatable {

    public Diploma getCertificate(User user);

}
