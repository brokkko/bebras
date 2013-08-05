package models.results;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.List;

/**
 * Created by ilya
 */
public class EmptyTranslator implements Translator {
    @Override
    public Info translate(List<Info> from, List<Info> settings) {
        return new Info();
    }

    @Override
    public InfoPattern getInfoPattern() {
        return new InfoPattern();
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
