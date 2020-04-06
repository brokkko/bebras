package models.newproblems.kio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.ServerConfiguration;
import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.*;
import models.results.Info;
import models.results.InfoPattern;
import play.Logger;
import play.twirl.api.Html;
import ru.ipo.kio.js.JsKioProblem;
import ru.ipo.kio.js.Parameter;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 23:17
 */
public class KioOnlineProblem implements Problem {

    private static final char[] UNIQUE_ID_CHARS;
    private static final Random rnd = new Random();

    static {
        UNIQUE_ID_CHARS = new char[26 + 26];
        int ind = 0;
        for (char c = 'a'; c <= 'z'; c++)
            UNIQUE_ID_CHARS[ind++] = c;
        for (char c = 'A'; c <= 'Z'; c++)
            UNIQUE_ID_CHARS[ind++] = c;
    }

    private String title;
    private String statement;
    private String help;
    private String className;
    private String settings;
    private String dependencies;
    private String checker;

    private JsKioProblem jsKioProblem;

    private ObjectMapper objectMapper = new ObjectMapper();

    public KioOnlineProblem() {
    }

    private String randomString(int len) {
        char[] result = new char[len];

        for (int i = 0; i < len; i++)
            result[i] = UNIQUE_ID_CHARS[rnd.nextInt(UNIQUE_ID_CHARS.length)];

        return new String(result);
    }

    public String getClassName() {
        return className;
    }

    public String getSettings() {
        return settings;
    }

    public String getDependencies() {
        return dependencies;
    }

    public String getChecker() {
        return checker;
    }

    @Override
    public Html format(String index, boolean showSolutions, Info settings, long randSeed) {
        String uniqueId = randomString(20);

        return views.html.kio.kio_online_problem.render(
                showSolutions, uniqueId,
                title, statement, help, className, this.settings, dependencies
        );
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public Html formatEditor() {
        return views.html.kio.kio_online_editor.render(
                title, statement, help, className, settings, dependencies, checker
        );
    }

    @Override
    public void updateProblem(RawForm form) {
        title = form.get("title");
        statement = form.get("statement");
        help = form.get("help");
        className = form.get("class_name");
        settings = form.get("settings");
        dependencies = form.get("dependencies");
        checker = form.get("checker");

        jsKioProblem = evalJsKioProblem();
    }

    @Override
    public String answerToString(Info answer, long randSeed) {
        if (answer == null)
            return "-";

        return answer.get("res") + " -> " + answer.get("sol");
    }

    @Override
    public String answerString() {
        return ".";
    }

    @Override
    public Info check(Info answer, long randSeed) {
        if (answer == null)
            return null;
        String resultJson = (String) answer.get("res");

        return check(resultJson);
    }

    public Info check(String resultJson) {
        Info checkResult = new Info();

        JsonNode jsonResult;
        try {
            jsonResult = objectMapper.readTree(resultJson);
        } catch (IOException e) {
            //TODO do we need to log here?
            return checkResult;
        }

        jsonResult.fields().forEachRemaining(e -> checkResult.put(e.getKey(), decode(e.getValue())));

        //checkResult: real param name -> decoded value of real type

        List<Parameter> visibleParameters = getVisibleParameters();

        Info result = new Info();

        int n = visibleParameters.size();
        for (int i = 0; i < n; i++) {
            Parameter param = visibleParameters.get(i);
            String paramName = "p" + i;
            Object paramValue = checkResult.get(param.name());
            result.put(paramName, paramValue == null ? "-" : param.v(paramValue)); //TODO process null on view level
        }

        if (jsKioProblem == null)
            throw new IllegalStateException("jsKioProblem == null");
        List<Parameter> params = jsKioProblem.getParameters();
        List<Double> rankSorterValue = params.stream().map(p -> {
            Object paramValue = checkResult.get(p.name());
            return paramValue == null ? -Double.MAX_VALUE : p.normalizeWithOrdering(paramValue);
        }).collect(Collectors.toList());
        result.put("rank-sorter", rankSorterValue);

        return result;
    }

    public List<Parameter> getVisibleParameters() {
        return jsKioProblem.getParameters() //TODO invisible parameters are defined in KioAPI
                .stream()
                .filter(p -> p.title() != null && !p.title().isEmpty())
                .collect(Collectors.toList());
    }

    private Object decode(JsonNode value) {
        if (value.isLong())
            return value.asLong();
        if (value.isInt())
            return value.asInt();
        if (value.isNumber())
            return value.asDouble();
        if (value.isBoolean())
            return value.asBoolean() ? 1 : 0;
        return value.asText(); //TODO ???
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return new InfoPattern(
                "sol", new BasicSerializationType<>(String.class), "solution",
                "res", new BasicSerializationType<>(String.class), "result"
        );
    }

    @Override
    public InfoPattern getCheckerPattern() {
        if (jsKioProblem == null)
            return new InfoPattern();

        LinkedHashMap<String, SerializationType<?>> field2type = new LinkedHashMap<>();
        LinkedHashMap<String, String> field2title = new LinkedHashMap<>();

        List<Parameter> visibleParameters = getVisibleParameters();

        int i = 0;
        for (Parameter parameter : visibleParameters) {
            String parameterName = "p" + i;
            i++;
            field2title.put(parameterName, parameter.title());
            field2type.put(parameterName, new BasicSerializationType<>(String.class));
        }

        field2title.put("rank-sorter", "Сортировка");
        field2type.put("rank-sorter", new ListSerializationType<>(new BasicSerializationType<>(double.class)));

        return new InfoPattern(field2type, field2title);
    }

    @Override
    public String getType() {
        return "kio-online";
    }

    @Override
    public Widget getWidget(boolean editor) {
        ListWidget w = new ListWidget(
                new ResourceLink("kio_api.css"),
                new ResourceLink("kio_online.problem.css"),
                new ResourceLink("preloadjs-0.6.2.min.js"),
                new ResourceLink("kio_api.js")
        );

        if (editor)
            w = w.add(new ResourceLink("bebras.edit.problem.js"));

        String urlPrefix = "../../~plugin/KioOnline/";

        if (!"".equals(dependencies))
            for (String dependency : dependencies.split(","))
                w = w.add(new ResourceLink(urlPrefix + dependency.trim()));

        return w;
    }

    public JsKioProblem getJsKioProblem() {
        return jsKioProblem;
    }

    public JsKioProblem evalJsKioProblem() {
        String className = getClassName();
        String settings = getSettings();
        String dependencies = getDependencies();

        //TODO we assume, file with task is the first in the list of dependencies
        String[] jsCodes = dependencies.split("\\s*,\\s*");
        if (jsCodes.length == 0)
            return null;

        File kioOnlineFolder = ServerConfiguration.getInstance().getPluginFolder("KioOnline");

        StringBuilder jsCode = new StringBuilder();
        for (String jsCodeFilename : jsCodes) {
            if (!jsCodeFilename.toLowerCase().endsWith(".js") || jsCodeFilename.toLowerCase().startsWith("easeljs"))
                continue;
            File jsCodeFile = new File(kioOnlineFolder, jsCodeFilename);
            try {
                byte[] bytes = Files.readAllBytes(jsCodeFile.toPath());
                jsCode.append(new String(bytes, StandardCharsets.UTF_8));
                jsCode.append(" ");
            } catch (IOException e) {
                Logger.error("Failed to read file " + jsCodeFile, e);
                return null;
            }
        }

        String fixedJsCode = "createjs = {Container: {}, Bitmap: {}, Shape: {}, Event: {}, EventDispatcher: {}}; " + jsCode;

        try {
            return new JsKioProblem(fixedJsCode, className, settings, null); //TODO add external checker
        } catch (Exception e) {
            throw new IllegalStateException("failed to load js code", e);
        }
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
            int n2 = r2.size();
            if (n2 != n) //TODO why is this possible, seems, that data may change by itself (n2 = 0 sometimes)
                return n - n2;
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

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
        serializer.write("statement", statement);
        serializer.write("help", help);
        serializer.write("className", className);
        serializer.write("settings", settings);
        serializer.write("dependencies", dependencies);
        serializer.write("checker", checker);
    }

    @Override
    public void update(Deserializer deserializer) {
        title = deserializer.readString("title", "");
        statement = deserializer.readString("statement", "");
        help = deserializer.readString("help", "");
        className = deserializer.readString("className", "");
        settings = deserializer.readString("settings", "");
        dependencies = deserializer.readString("dependencies", "");
        checker = deserializer.readString("checker", "");

        jsKioProblem = evalJsKioProblem();
    }
}
