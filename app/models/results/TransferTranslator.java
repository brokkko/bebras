package models.results;

import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 29.08.13
 * Time: 2:24
 */
public class TransferTranslator implements Translator {

    private String userField;
    private String infoField;
    private String title;

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        return new Info(infoField, user.getInfo().get(userField));
    }

    @Override
    public InfoPattern getInfoPattern() {
        //TODO think about title
        return new InfoPattern(infoField, new BasicSerializationType<>(String.class), title);
    }

    @Override
    public void serialize(Serializer serializer) {
        if (userField != null && userField.equals(infoField))
            serializer.write("field", userField);
        else {
            serializer.write("user field", userField);
            serializer.write("info field", infoField);
        }

        serializer.write("title", title);
    }

    @Override
    public void update(Deserializer deserializer) {
        String field = deserializer.readString("field");

        if (field != null) {
            userField = field;
            infoField = field;
        } else {
            userField = deserializer.readString("user field");
            infoField = deserializer.readString("info field");
        }

        title = deserializer.readString("title");
    }
}
