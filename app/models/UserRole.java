package models;

import models.newserialization.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 21.08.13
 * Time: 0:19
 */
public class UserRole implements SerializableUpdatable {

    public static final UserRole EMPTY = new UserRole();

    private String name;
    private Set<String> rights;

    public boolean hasRight(String right) {
        return rights != null && right.contains(right);
    }

    public String getName() {
        return name;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        SerializationTypesRegistry.list(String.class).write(serializer, "rights", new ArrayList<>(rights));
    }

    @Override
    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        rights = new HashSet<>(SerializationTypesRegistry.list(String.class).read(deserializer, "rights"));
    }
}
