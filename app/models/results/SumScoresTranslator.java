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
public class SumScoresTranslator implements Translator {

    private static final InfoPattern infoPattern = new InfoPattern(
            "scores",
            new BasicSerializationType<>(int.class),
            Messages.get(Lang.defaultLang(), "results_translator.sum_scores.title.scores")
    );

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
        return infoPattern;
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return new InfoPattern(); //TODO allow weight for scores
    }

    @Override
    public void serialize(Serializer serializer) {
        //do nothing
    }

    @Override
    public void update(Deserializer deserializer) {
        //do nothing
    }
}
