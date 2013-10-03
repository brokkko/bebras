package models.results;

import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 12.08.13
 * Time: 23:44
 */
public class CombinedTranslator implements Translator {

    private List<Translator> translators;

    public CombinedTranslator() {
    }

    public CombinedTranslator(List<Translator> translators) {
        this.translators = translators;
    }

    public void addTranslator(Translator translator) {
        translators.add(translator);
    }


    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        Info result = new Info();

        for (Translator translator : translators) {
            Info info = translator.translate(from, settings, user);
            for (String field : translator.getInfoPattern().getFields())
                result.put(field, info.get(field));
        }

        return result;
    }

    @Override
    public InfoPattern getInfoPattern() {
        InfoPattern result = new InfoPattern();

        for (Translator translator : translators) {
            InfoPattern infoPattern = translator.getInfoPattern();
            for (String field : infoPattern.getFields())
                result.register(
                        field,
                        infoPattern.getType(field),
                        infoPattern.getTitle(field)
                );
        }

        return result;
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        if (translators == null)
            return new InfoPattern();

        InfoPattern result = null;

        for (Translator translator : translators) {
            InfoPattern next = translator.getConfigInfoPattern();

            if (result == null)
                result = next;
            else
                result = InfoPattern.union(result, next);
        }

        return result == null ? new InfoPattern() : result;
    }

    @Override
    public void serialize(Serializer serializer) {
        //do nothing //TODO implement, we don't need this functionality now
    }

    @Override
    public void update(Deserializer deserializer) {
        //do nothing //TODO implement, we don't need this functionality now
    }

    public List<Translator> getTranslators() {
        List<Translator> tr = translators;

        if (tr.size() == 1 && tr.get(0) instanceof EmptyTranslator)
            return new ArrayList<>();

        return tr;
    }
}
