package models.data;

import models.applications.ApplicationsProviderFactory;
import models.newserialization.SerializableTreeSerializationType;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.07.13
 * Time: 1:23
 */
public class ObjectsProviderFactorySerializationType extends SerializableTreeSerializationType<ObjectsProviderFactory> {

    public ObjectsProviderFactorySerializationType() {
        registerClass("users", UsersProviderFactory.class);
        registerClass("apps", ApplicationsProviderFactory.class);
    }

}
