package models.results;

import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.i18n.Messages;

import java.util.List;

/**
 * Created by ilya
 */
public class BeaverTranslator implements Translator {

    private int scores;
    private int penalty;
    private int noAnswerPenalty;
    private boolean noLessThan0 = false;

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        int problemsCount = from.size();

        int sum = 0;
        int r = 0;
        int w = 0;
        int n = 0;
//        StringBuilder ans = new StringBuilder();

        for (int i = 0; i < problemsCount; i++) {
            Info problemResult = from.get(i);
            Info problemSettings = settings.get(i);

            int result = problemResult == null ? 0 : (Integer) problemResult.get("result");
//            String answer = problemResult == null ? "." : (String) problemResult.get("answer");

            int localScores = problemSettings == null ? 1 : problemSettings.get("r") == null ? 1 : (Integer) problemSettings.get("r");
            int localPenalty = problemSettings == null ? -1 : problemSettings.get("w") == null ? -1 : (Integer) problemSettings.get("w");

            if (result < 0) {
                result = penalty * localPenalty * result;
//                    ans.append(answer.toLowerCase());
                w++;
            } else if (result > 0) {
                result = scores * localScores * result;
//                    ans.append(answer.toUpperCase());
                r++;
            } else {
                result = noAnswerPenalty;
//                    ans.append('.');
                n++;
            }

            sum += result;
        }

        Info result = new Info();
        if (noLessThan0)
            result.put("scores", sum < 0 ? 0 : sum);
        else
            result.put("scores", sum);
        result.put("r", r);
        result.put("w", w);
        result.put("n", n);
//        result.put("ans", ans.toString());
//        result.put("max", problemsCount * scores);
        return result;
    }

    @Override
    public InfoPattern getInfoPattern() {
        return new InfoPattern(
                "scores", new BasicSerializationType<>(int.class), Messages.get("results_translator.beaver.title.scores"),
                "r", new BasicSerializationType<>(int.class), Messages.get("results_translator.beaver.title.right"),
                "w", new BasicSerializationType<>(int.class), Messages.get("results_translator.beaver.title.wrong"),
                "n", new BasicSerializationType<>(int.class), Messages.get("results_translator.beaver.title.skip")/*,
                "ans", new BasicSerializationType<>(String.class), Messages.get("results_translator.beaver.title.answers"),
                "max", new BasicSerializationType<>(int.class), Messages.get("results_translator.beaver.title.max")*/
        );
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return new InfoPattern(
                                      "r", new BasicSerializationType<>(int.class), "Баллов за правильный ответ",
                                      "w", new BasicSerializationType<>(int.class), "Баллов за неправильный ответ"
        );
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("scores", scores);
        serializer.write("penalty", penalty);
        serializer.write("no ans", noAnswerPenalty);
        serializer.write("no less 0", noLessThan0);
    }

    @Override
    public void update(Deserializer deserializer) {
        scores = deserializer.readInt("scores", 1);
        penalty = deserializer.readInt("penalty", -1);
        noAnswerPenalty = deserializer.readInt("no ans", 0);
        noLessThan0 = deserializer.readBoolean("no less 0", false);
    }
}
