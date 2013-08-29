package models;

import models.forms.InputField;
import models.forms.InputForm;
import models.newserialization.*;
import models.results.InfoPattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private String name;
    private Set<String> rights;
    private InputForm usersForm;
    private InfoPattern userInfoPattern;

    private String title;
    private String description;

    private List<String> mayRegister = new ArrayList<>();

    public UserRole() {
    }

    public boolean hasRight(String right) {
        return rights != null && rights.contains(right);
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
