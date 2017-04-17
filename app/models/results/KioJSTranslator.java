package models.results;

import models.Contest;
import models.ServerConfiguration;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.kio.KioOnlineProblem;
import models.newserialization.*;
import play.Logger;
import ru.ipo.kio.js.JsKioProblem;
import ru.ipo.kio.js.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KioJSTranslator implements Translator {

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
        visibleParameters.forEach(p -> result.put(
                p.name(),
                p.v(source.get(p.name()))
        ));

        return result;
    }

    @Override
    public InfoPattern getInfoPattern() {
        if (problem == null)
            return new InfoPattern();

        Map<String, String> field2title = new LinkedHashMap<>();
        Map<String, SerializationType<?>> field2type = new LinkedHashMap<>();

        //problem rank-sorter parameters
        String sortingParamName = "rank-sorter";
        field2title.put(sortingParamName, "");
        field2type.put(sortingParamName, SerializationTypesRegistry.list(new BasicSerializationType<>(Integer.class)));

        //problem rank parameter
        String rankParamName = "rank";
        field2title.put(rankParamName,"Ранг");
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
        Problem mainProblem = mainConfiguredProblem.getProblem();
        if (!(mainProblem instanceof KioOnlineProblem))
            return;

        KioOnlineProblem kop = (KioOnlineProblem) mainProblem;
        String className = kop.getClassName();
        String settings = kop.getSettings();
        String dependencies = kop.getDependencies();

        //TODO we assume, file with task is the first in the list of dependencies
        String[] jsCodes = dependencies.split("\\s*,\\s*");
        if (jsCodes.length == 0)
            return;
        String jsCodeFilename = jsCodes[0];
        File kioOnlineFolder = ServerConfiguration.getInstance().getPluginFolder("KioOnline");
        File jsCodeFile = new File(kioOnlineFolder, jsCodeFilename);
        String jsCode;
        try {
            byte[] bytes = Files.readAllBytes(jsCodeFile.toPath());
            jsCode = new String(bytes, "UTF-8");
        } catch (IOException e) {
            Logger.error("Failed to read file " + jsCodeFile, e);
            return;
        }

        problem = new JsKioProblem(jsCode, className, settings);

        visibleParameters = problem.getParameters() //TODO invisible parameters are defined in KioAPI
                .stream()
                .filter(p -> p.title() != null && !p.title().isEmpty())
                .collect(Collectors.toList());
    }
}
