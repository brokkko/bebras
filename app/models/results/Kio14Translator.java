package models.results;

import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.List;

public class Kio14Translator implements Translator {
    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        String gradeS = (String) user.getInfo().get("grade");
        int grade = 0;
        try {
            grade = Integer.parseInt(gradeS);
        } catch (NumberFormatException ignored) {
        }
        int level;
        if (grade < 5)
            level = 0;
        else if (grade > 7)
            level = 2;
        else
            level = 1;

        Info result = new Info();

        String prefix = "kio_" + level + "_";
        for (String field : new String[]{
                "has_intersected_lines",
                "total_number_of_difference_graphs",
                "total_number_of_right_graphs",
                "sum_of_lines",
                "scores_stars",
                "total_length",
                "scores_peterhof",
                "statements",
                "figures",
                "length",
                "scores_tarski",
                "scores",
                "rank"
        } )
            result.put(field, user.getInfo().get(prefix + field));

        return result;
    }

    @Override
    public InfoPattern getInfoPattern() {
        return new InfoPattern(
                "rank", new BasicSerializationType<>(String.class), "Место",
                "scores", new BasicSerializationType<>(String.class), "Баллы",

                "total_number_of_difference_graphs", new BasicSerializationType<>(String.class), "Различных созвездий",
                "total_number_of_right_graphs", new BasicSerializationType<>(String.class), "Всего созвездий",
                "sum_of_lines", new BasicSerializationType<>(String.class), "Длина линий",

                "scores_stars", new BasicSerializationType<>(String.class), "Баллы (Звезды)",


                "total_length", new BasicSerializationType<>(String.class), "Длина струй",

                "scores_peterhof", new BasicSerializationType<>(String.class), "Баллы (Фонтаны)",


                "statements", new BasicSerializationType<>(String.class), "Выполнено утверждений",
                "figures", new BasicSerializationType<>(String.class), "Установлено фигурок",
                "length", new BasicSerializationType<>(String.class), "Использовано условий",

                "scores_tarski", new BasicSerializationType<>(String.class), "Баллы (Логика)"
        );
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return new InfoPattern();
    }

    @Override
    public void serialize(Serializer serializer) {

    }

    @Override
    public void update(Deserializer deserializer) {

    }
}
