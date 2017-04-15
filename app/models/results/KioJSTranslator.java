package models.results;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.List;

public class KioJSTranslator implements Translator {
    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        return null;
    }

    @Override
    public InfoPattern getInfoPattern() {
        return null;
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return null;
    }

    @Override
    public void update(Deserializer deserializer) {

    }

    @Override
    public void serialize(Serializer serializer) {

    }
}
