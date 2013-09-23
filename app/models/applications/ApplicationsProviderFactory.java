package models.applications;

import models.Event;
import models.User;
import models.data.ObjectsProvider;
import models.data.ObjectsProviderFactory;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.09.13
 * Time: 2:32
 */
public class ApplicationsProviderFactory implements ObjectsProviderFactory<ApplicationWithUser> {

    private static final List<String> FIELDS = Arrays.asList("state", "code", "login");
    private static final List<String> TITLE = Arrays.asList("Статус (0, 1, 2)", "Код заявки", "login");

    private String role;

    @Override
    public ObjectsProvider<ApplicationWithUser> get(Event currentEvent, User currentUser, List<String> searchFields, List<String> searchValues) {
        int state = -1;
        String name = null;
        String login = null;

        for (int i = 0; i < searchFields.size(); i++) {
            String searchField = searchFields.get(i);
            String value = searchValues.get(i);
            switch (searchField) {
                case "state":
                    try {
                        state = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                case "name":
                    name = value;
                    break;
                case "login":
                    login = value;
                    break;
            }
        }

        return new ApplicationsProvider(currentEvent, currentUser, role, state, name, login);
    }

    @Override
    public Class<ApplicationWithUser> getObjectsClass() {
        return ApplicationWithUser.class;
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    @Override
    public List<String> getTitles() {
        return TITLE;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("role", role);
    }

    @Override
    public void update(Deserializer deserializer) {
        role = deserializer.readString("role");
    }
}
