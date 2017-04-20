package models.results.kio;

import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationType;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import models.results.Translator;
import plugins.kio.KioParameter;
import plugins.kio.KioProblemSet;

import java.util.*;

public class KioTranslator implements Translator {

    private int year;

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        int level = getKioLevel(user);
        Info result = new Info();
        result.put("level", "" + level);
        String prefix = "kio_" + level + "_";

        for (String field : getInfoPattern().getFields())
            if (!field.equals("level")) {
                if (field.startsWith("delimiter__"))
                    result.put(field, " ");
                else
                    result.put(field, user.getInfo().get(prefix + field));
            }

        return result;
    }

    private int getKioLevel(User user) {
        String levelS = (String) user.getInfo().get("kio_level");
        try {
            return Integer.parseInt(levelS);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public InfoPattern getInfoPattern() {
        InfoPattern contest = new InfoPattern(
                "level", new BasicSerializationType<>(String.class), "Уровень",
                "rank", new BasicSerializationType<>(String.class), "Место",
                "scores", new BasicSerializationType<>(String.class), "Баллы"
        );

        KioProblemSet kioProblemSet = KioProblemSet.getInstance(year);

        List<String> pids = new ArrayList<>();
        for (int level = 0; level <= 2; level++) {
            Set<String> levelPids = new LinkedHashSet<>();
            for (String pid : kioProblemSet.getProblemIds(level))
                levelPids.add(pid);
            pids.addAll(levelPids);
        }

        LinkedHashMap<String, SerializationType<?>> field2type = new LinkedHashMap<>();
        LinkedHashMap<String, String> field2title = new LinkedHashMap<>();

        for (String pid : pids) {
            field2title.put("delimiter__" + pid, "-");
            field2type.put("delimiter__" + pid, new BasicSerializationType<>(String.class));

            Set<String> names = new LinkedHashSet<>();

            for (int level = 0; level <= 2; level++) {
                String name = kioProblemSet.getProblemName(level, pid);
                names.add(name);
            }
            String name = join(names, ", ");

            field2title.put("scores_" + pid, "Баллы (" + name + ")");
            field2type.put("scores_" + pid, new BasicSerializationType<>(String.class));
            field2title.put("rank_" + pid, "Место (" + name + ")");
            field2type.put("rank_" + pid, new BasicSerializationType<>(String.class));

            Set<KioParameter> params = new LinkedHashSet<>();
            //get all params from pids
            for (int level = 0; level <= 2; level++)
                for (KioParameter param : kioProblemSet.getParams(level, pid))
                    params.add(param);

            for (KioParameter param : params) {
                field2title.put(pid + "_" + param.getId(), param.getName());
                field2type.put(pid + "_" + param.getId(), new BasicSerializationType<>(String.class));
            }

            field2title.put("scores_" + pid, "Баллы (" + name + ")");
            field2type.put("scores_" + pid, new BasicSerializationType<>(String.class));
            field2title.put("rank_" + pid, "Место (" + name + ")");
            field2type.put("rank_" + pid, new BasicSerializationType<>(String.class));
        }
        return InfoPattern.union(contest, new InfoPattern(field2type, field2title));
    }

    private String join(Collection<String> names, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if (sb.length() != 0)
                sb.append(delimiter);
            sb.append(name);
        }
        return sb.toString();
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return new InfoPattern();
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("year", year);
    }

    @Override
    public void update(Deserializer deserializer) {
        year = deserializer.readInt("year", 2014);
    }
}
