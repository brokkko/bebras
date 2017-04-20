package models.results;

import models.Contest;
import models.User;
import models.newserialization.SerializableUpdatable;

import java.util.Comparator;
import java.util.List;

/**
 * Created by ilya
 */
public interface Translator extends SerializableUpdatable {

    Info translate(List<Info> from, List<Info> settings, User user);
    InfoPattern getInfoPattern();
    InfoPattern getConfigInfoPattern();

    //TODO all next methods should be somewhere else

    default void setup(Contest contest) {} //TODO this is about setting up a contest, but it can not be only contest

    default <T> void updateFromPreorder(Info results, Preorder<T> preorder, int level) {
    }

    default Comparator<Info> comparator() {
        return null;
    }

    default Object getUserType(User user) {
        return null;
    }
}
