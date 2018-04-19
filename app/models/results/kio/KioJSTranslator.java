package models.results.kio;

import models.Contest;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.kio.KioOnlineProblem;
import models.newserialization.*;
import models.results.Info;
import models.results.InfoPattern;
import models.results.Preorder;
import models.results.Translator;
import play.Logger;
import ru.ipo.kio.js.JsKioProblem;
import ru.ipo.kio.js.Parameter;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class KioJSTranslator implements Translator {

    public static final ListSerializationType<Double> RANK_SORTER_TYPE = SerializationTypesRegistry.list(new BasicSerializationType<>(Double.class));
    private KioOnlineProblem mainProblem = null;
    private JsKioProblem problem = null;
    private List<Parameter> visibleParameters;

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        if (from.size() != 1) {
            Logger.warn("kio js translator can translate exactly one problem in contest");
            return new Info();
        }

        Info source = from.get(0);

        Info result = new Info();
        List<Parameter> visibleParameters = mainProblem.getVisibleParameters();
        int n = visibleParameters.size();
        for (int i = 0; i < n; i++) {
            String paramName = "p" + i;
            Object paramValue = source == null ? null : source.get(paramName);
            result.put(paramName, paramValue); //TODO process null on view level
        }

        if (source != null)
            result.put("rank-sorter", source.get("rank-sorter"));
        else {
            List<Double> sorter = Collections.nCopies(problem.getParameters().size(), -Double.MAX_VALUE);
            result.put("rank-sorter", sorter);
        }
        result.put("rank", 0);
        result.put("scores", 0);

        return result;
    }

    @Override
    public InfoPattern getInfoPattern() {
        if (problem == null)
            return new InfoPattern();

        LinkedHashMap<String, String> field2title = new LinkedHashMap<>();
        LinkedHashMap<String, SerializationType<?>> field2type = new LinkedHashMap<>();

        //problem rank-sorter parameters
        String sortingParamName = "rank-sorter";
        field2title.put(sortingParamName, "");
        field2type.put(sortingParamName, RANK_SORTER_TYPE);

        //problem rank parameter
        String rankParamName = "rank";
        field2title.put(rankParamName,"Место");
        field2type.put(rankParamName, new BasicSerializationType<>(Integer.class));

        //problem scores parameter
        String scoresParamName = "scores";
        field2title.put(scoresParamName, "Баллов");
        field2type.put(scoresParamName, new BasicSerializationType<>(Integer.class));

        //normal parameters
        int n = visibleParameters.size();
        for (int i = 0; i < n; i++) {
            String paramName = "p" + i;
            Parameter param = visibleParameters.get(i);
            field2title.put(paramName, param.title());
            field2type.put(paramName, new BasicSerializationType<>(String.class));
        }

        return new InfoPattern(field2type, field2title);
    }

    @Override
    public InfoPattern getConfigInfoPattern() {
        return new InfoPattern();
    }

    @Override
    public void update(Deserializer deserializer) {
        Logger.debug("updating kio js translator");
    }

    @Override
    public void serialize(Serializer serializer) {}

    @Override
    public void setup(Contest contest) {
        List<ConfiguredProblem> allPossibleProblems = contest.getAllPossibleProblems();
        if (allPossibleProblems == null || allPossibleProblems.isEmpty())
            return;
        ConfiguredProblem mainConfiguredProblem = allPossibleProblems.get(0);
        Problem mainProblem1 = mainConfiguredProblem.getProblem();
        if (!(mainProblem1 instanceof KioOnlineProblem))
            return;

        mainProblem = (KioOnlineProblem) mainProblem1;

        KioOnlineProblem kop = (KioOnlineProblem) mainProblem;
        try {
            problem = kop.getJsKioProblem();
        } catch (Exception e) {
            Logger.error("Error while loading kio-online problem", e);
            return;
        }
        if (problem == null) return;

        visibleParameters = problem.getParameters() //TODO invisible parameters are defined in KioAPI
                .stream()
                .filter(p -> p.title() != null && !p.title().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public <T> void updateFromPreorder(Info results, Preorder<T> preorder, int level) {
        results.put("scores", level == 0 ? 0 : preorder.getAccumulatedLevelSize(level - 1));
        results.put("rank", preorder.getLevelsCount() - level);
    }

    @Override
    public Comparator<Info> comparator() {
        return (info1, info2) -> {
            List<Double> r1 = (List<Double>) info1.get("rank-sorter");
            List<Double> r2 = (List<Double>) info2.get("rank-sorter");

            if (r1 == null && r2 == null)
                return 0;
            if (r1 == null)
                return -1;
            if (r2 == null)
                return 1;

            int n = r1.size();
            for (int i = 0; i < n; i++) {
                double diff = r1.get(i) - r2.get(i);
                if (Math.abs(diff) < 1e-8)
                    continue;
                if (diff < 0)
                    return -1;
                return 1;
            }

            return 0;
        };
    }

    //TODO user type should be a field in translator
    @Override
    public Object getUserType(User user) {
        int level = KioLevelTranslator.grade2level(user);
        if (level < 0)
            return null;
        else
            return level;
    }
}
