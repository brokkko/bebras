package models.results;

import models.Contest;
import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.i18n.Lang;
import play.i18n.Messages;

import java.util.Comparator;
import java.util.List;

/**
 * Created by ilya
 */
public class SumScoresTranslator implements Translator {

    private String scoresField = "scores";
    private static final String rankField = "rank";
    private boolean showRank = false;

    private InfoPattern infoPattern() {
        return new InfoPattern(
                scoresField,
                new BasicSerializationType<>(int.class),
                Messages.get(new Lang(Lang.defaultLang()), "results_translator.sum_scores.title.scores")
        );
    }

    private InfoPattern infoPatternWithRank() {
        return InfoPattern.union(infoPattern(),
                new InfoPattern(
                        rankField,
                        new BasicSerializationType<>(int.class),
                        Messages.get(new Lang(Lang.defaultLang()), "Место")
                )
        );
    }

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        int scores = 0;
        for (Info entry : from) {
            Object sc = entry.get("scores");
            scores += sc == null ? 0 : (Integer) sc;
        }

        return new Info("scores", scores);
    }

    @Override
    public InfoPattern getInfoPattern() {
        if (showRank)
            return infoPatternWithRank();
        else
            return infoPattern();
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return new InfoPattern(); //TODO allow weight for scores
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("rank", showRank);
        serializer.write("field", scoresField);
    }

    @Override
    public void update(Deserializer deserializer) {
        showRank = deserializer.readBoolean("rank", false);
        scoresField = deserializer.readString("field", "scores");
    }

    @Override
    public <T> void updateFromPreorder(Info results, Preorder<T> preorder, int level) {
        results.put("rank", preorder.getLevelsCount() - level);
    }

    @Override
    public Comparator<Info> comparator() {
        if (showRank)
            return (info, t1) -> {
                int scores1 = (Integer) info.getOrDefault(scoresField, 0);
                int scores2 = (Integer) t1.getOrDefault(scoresField, 0);
                return scores1 - scores2;
            };
        else
            return null;
    }
}
