package models.results.kio;

import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import models.results.Translator;

import java.util.List;

public class KioLevelTranslator implements Translator {

    public static int grade2level(User user) {
        String grade = (String) user.getInfo().get("grade");
        if (grade == null)
            return -1;

        if (!grade.matches("[0-9]{1,2}"))
            return -1;
        int g = Integer.parseInt(grade);

        if (g <= 4)
            return 0;
        if (g <= 7)
            return 1;
        return 2;
    }

    @Override
    public void update(Deserializer deserializer) {

    }

    @Override
    public void serialize(Serializer serializer) {

    }

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        int level = grade2level(user);
        Info result = new Info();

        if (level < 0)
            result.put("kiolevel", "-");
        else
            result.put("kiolevel", "" + level);

        return result;
    }

    @Override
    public InfoPattern getInfoPattern() {
        return new InfoPattern(
                "kiolevel",
                new BasicSerializationType<>(String.class),
                "Уровень"
        );
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return null;
    }


}
