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
import ru.ipo.kio.js.Result;
import scala.collection.JavaConverters$;
import scala.collection.Seq;
import scala.collection.convert.Decorators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KioJSTranslator implements Translator {

    private JsKioProblem problem = null;

    @Override
    public Info translate(List<Info> from, List<Info> settings, User user) {
        if (from.size() != 1) {
            Logger.warn("kio js translator can translate exactly one problem in contest");
            return new Info();
        }

        return from.get(0);
    }

    @Override
    public InfoPattern getInfoPattern() {
        if (problem == null)
            return new InfoPattern();

        List<Parameter> parameters = problem.getParameters();
        Map<String, String> field2title = parameters
                .stream()
                .collect(Collectors.toMap(
                        Parameter::name,
                        Parameter::title,
                        (u, v) -> {throw new IllegalStateException("two same keys in list");},
                        LinkedHashMap::new
                ));
        Map<String, SerializationType<?>> field2type = parameters
                .stream()
                .collect(Collectors.toMap(
                        Parameter::name,
                        parameter -> new BasicSerializationType<>(String.class),
                        (u, v) -> {throw new IllegalStateException("two same keys in list");},
                        LinkedHashMap::new
                ));

        //TODO add field about sorting

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
    }
}
