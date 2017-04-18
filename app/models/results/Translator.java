package models.results;

import models.Contest;
import models.User;
import models.newserialization.SerializableUpdatable;

import java.util.List;

/**
 * Created by ilya
 */
public interface Translator extends SerializableUpdatable {

    Info translate(List<Info> from, List<Info> settings, User user);
    InfoPattern getInfoPattern();
    InfoPattern getConfigInfoPattern();

    default void setup(Contest contest) {} //TODO this is about setting up a contest, but it can not be only contest

    default void setScoresAndRank(Info results, int scores, int rank) {
    }
}
