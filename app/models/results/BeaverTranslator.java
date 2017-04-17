package models.results;

import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.i18n.Lang;
import play.i18n.Messages;

import java.util.List;

/**
 * Created by ilya
 */
public class BeaverTranslator implements Translator {

    private static final String MSG_SCORES = mGet("results_translator.beaver.title.scores");
    private static final String MSG_RIGHT = mGet("results_translator.beaver.title.right");
    private static final String MSG_WRONG = mGet("results_translator.beaver.title.wrong");
    private static final String MSG_SKIP = mGet("results_translator.beaver.title.skip");
    private static final String MSG_SCORES_RIGHT = mGet("results_translator.beaver.title.scores_right");
    private static final String MSG_SCORES_WRONG = mGet("results_translator.beaver.title.scores_wrong");

    private static String mGet(String field) {
        return Messages.get(new Lang(Lang.defaultLang()), field); //TODO wtf default lang
    }

    private int scores;
    private int penalty;
    private int noAnswerPenalty;
    private boolean noLessThan0 = false;
    private boolean separateRightAndWrong = false;

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        int problemsCount = from.size();

        int sum = 0;
        int r = 0;
        int w = 0;
        int n = 0;
        int sumR = 0;
        int sumW = 0;

        for (int i = 0; i < problemsCount; i++) {
            Info problemResult = from.get(i);
            Info problemSettings = settings.get(i);

            int result = problemResult == null ? 0 : (Integer) problemResult.get("result");

            int localScores = problemSettings == null ? 1 : problemSettings.get("r") == null ? 1 : (Integer) problemSettings.get("r");
            int localPenalty = problemSettings == null ? -1 : problemSettings.get("w") == null ? -1 : (Integer) problemSettings.get("w");

            if (result < 0) {
                result = penalty * localPenalty * result;
                sumW += result;
                w++;
            } else if (result > 0) {
                result = scores * localScores * result;
                sumR += result;
                r++;
            } else {
                result = noAnswerPenalty;
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

        if (separateRightAndWrong) {
            result.put("rs", sumR);
            result.put("ws", sumW);
        }

        return result;
    }

    @Override
    public InfoPattern getInfoPattern() {
        InfoPattern infoPattern = new InfoPattern(
                "scores", new BasicSerializationType<>(int.class), MSG_SCORES,
                "r", new BasicSerializationType<>(int.class), MSG_RIGHT,
                "w", new BasicSerializationType<>(int.class), MSG_WRONG,
                "n", new BasicSerializationType<>(int.class), MSG_SKIP
        );

        if (separateRightAndWrong) {
            infoPattern.register("rs", new BasicSerializationType<>(int.class), MSG_SCORES_RIGHT);
            infoPattern.register("ws", new BasicSerializationType<>(int.class), MSG_SCORES_WRONG);
        }

        return infoPattern;
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
        serializer.write("separate right and wrong", separateRightAndWrong);
    }

    @Override
    public void update(Deserializer deserializer) {
        scores = deserializer.readInt("scores", 1);
        penalty = deserializer.readInt("penalty", -1);
        noAnswerPenalty = deserializer.readInt("no ans", 0);
        noLessThan0 = deserializer.readBoolean("no less 0", false);
        separateRightAndWrong = deserializer.readBoolean("separate right and wrong", false);
    }

}
