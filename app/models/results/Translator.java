package models.results;

import models.newserialization.SerializableUpdatable;

import java.util.List;

/**
 * Created by ilya
 */
public interface Translator extends SerializableUpdatable {

    Info translate(List<Info> from, List<Info> settings);
    InfoPattern getInfoPattern();

}
