package models;

import models.forms.InputField;
import models.forms.InputForm;
import models.newserialization.*;
import models.results.InfoPattern;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 21.08.13
 * Time: 0:19
 */
public class UserRole implements SerializableUpdatable {

    public static final UserRole EMPTY = new UserRole();

    static {
        EMPTY.update(new MemoryDeserializer("name", "EMPTY"));
    }

    public static final String DEFAULT_ENTER_URL = "contests";

    private String name;
    private Set<String> rights;
    private InputForm usersForm;
    private InfoPattern userInfoPattern;

    private String title;
    private String description;

    private String enterUrl;

    private List<String> mayRegister = new ArrayList<>();

    public UserRole() {
    }

    public boolean hasRight(String right) {
        return rights != null && rights.contains(right);
    }

    public Set<? extends String> getRights() {
//        return rights == null ? (Set<String>) Collections.emptySet() : rights; //TODO report: remove redundant leads to error, and no error reported
        return rights == null ? new HashSet<String>() : rights;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEnterUrl() {
        return enterUrl;
    }

    public InputForm getUsersForm() {
        return usersForm.filter(new InputForm.FieldFilter() {
            @Override
            public boolean accept(InputField field) {
                return !field.isExtra();
            }
        });
    }

    public InfoPattern getUserInfoPattern() {
        return userInfoPattern;
    }

    public InputForm getEditUserForm() {
        return usersForm.filter(new InputForm.FieldFilter() {
            @Override
            public boolean accept(InputField field) {
                return !field.isSkipForEdit() && field.isStore() && !field.isExtra();
            }
        });
    }

    public List<String> getMayRegister() {
        return mayRegister;
    }

    public boolean mayRegister(UserRole role) {
        return mayRegister.contains(role.getName());
    }

    public boolean mayRegisterSomebody() {
        return !mayRegister.isEmpty();
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        SerializationTypesRegistry.list(String.class).write(serializer, "rights", new ArrayList<>(rights));

        usersForm.serialize(serializer.getSerializer("info"));

        SerializationTypesRegistry.list(String.class).write(serializer, "may register", mayRegister);

        serializer.write("title", title);
        serializer.write("description", description);

        if (!DEFAULT_ENTER_URL.equals(enterUrl))
            serializer.write("enter", enterUrl);
    }

    @Override
    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        rights = new HashSet<>(SerializationTypesRegistry.list(String.class).read(deserializer, "rights"));

        InputForm usersForm = new SerializableSerializationType<>(InputForm.class).read(deserializer, "info");
        setUsersForm(usersForm);

        mayRegister = SerializationTypesRegistry.list(String.class).read(deserializer, "may register");

        title = deserializer.readString("title", name);
        description = deserializer.readString("description");

        enterUrl = deserializer.readString("enter", DEFAULT_ENTER_URL);
    }

    private void setUsersForm(InputForm usersForm) {
        this.usersForm = usersForm;

        if (this.usersForm == null)
            this.usersForm = new InputForm();

        //get info pattern from field
        userInfoPattern = new InfoPattern();
        if (this.usersForm != null)
            for (InputField inputField : this.usersForm.getFields())
                if (inputField.isStore()) {
                    if (inputField.isExtra())
                        //TODO think about title
                        //TODO extra fields are now only strings
                        userInfoPattern.register(inputField.getName(), new BasicSerializationType<>(String.class), inputField.getName());
                    else
                        userInfoPattern.register(inputField.getName(), inputField.getInputTemplate().getType(), inputField.getInputTemplate().getTitle());
                }
    }

}
