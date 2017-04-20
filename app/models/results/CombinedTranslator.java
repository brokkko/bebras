package models.results;

import models.Contest;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.ArrayList;
import java.util.Comparator;
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

    @Override
    public void setup(Contest contest) {
        for (Translator translator : translators)
            translator.setup(contest);
    }

    @Override
    public <T> void updateFromPreorder(Info results, Preorder<T> preorder, int level) {
        for (Translator translator : translators) {
            translator.updateFromPreorder(results, preorder, level);
        }
    }

    @Override
    //returns the first comparator out of several
    public Comparator<Info> comparator() {
        for (Translator translator : translators) {
            Comparator<Info> comparator = translator.comparator();
            if (comparator != null)
                return comparator;
        }
        return null;
    }

    @Override
    public Object getUserType(User user) {
        for (Translator translator : translators) {
            Object type = translator.getUserType(user);
            if (type != null)
                return type;
        }
        return null;
    }
}
