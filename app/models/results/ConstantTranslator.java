package models.results;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 29.08.13
 * Time: 2:24
 */
public class ConstantTranslator implements Translator {

//    String userField

    @Override
    public Info translate(List<Info> from, List<Info> settings) {
        return null;
    }

    @Override
    public InfoPattern getInfoPattern() {
        return new InfoPattern(

        );
    }

    @Override
    public void serialize(Serializer serializer) {

    }

    @Override
    public void update(Deserializer deserializer) {

    }
}
