package models.newproblems.kio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.ServerConfiguration;
import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationType;
import models.newserialization.Serializer;
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
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Random;

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
        Info checkResult = new Info(); //TODO what to do?

        String resultJson = (String) answer.get("res");
        JsonNode result;
        try {
            result = objectMapper.readTree(resultJson);
        } catch (IOException e) {
            //TODO do we need to log here?
            return checkResult;
        }

        result.fields().forEachRemaining(e -> checkResult.put(e.getKey(), decode(e.getValue())));

        return checkResult;
    }

    private Object decode(JsonNode value) {
        if (value.isLong())
            return value.asLong();
        if (value.isInt())
            return value.asInt();
        if (value.isNumber())
            return value.asDouble();
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

        for (Parameter parameter : jsKioProblem.getParameters())
            if (parameter.title() != null && !parameter.title().isEmpty()) {
                field2title.put(parameter.name(), parameter.title());
                field2type.put(parameter.name(), new BasicSerializationType<>(String.class));
            }

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
        String jsCodeFilename = jsCodes[0];
        File kioOnlineFolder = ServerConfiguration.getInstance().getPluginFolder("KioOnline");
        File jsCodeFile = new File(kioOnlineFolder, jsCodeFilename);
        String jsCode;
        try {
            byte[] bytes = Files.readAllBytes(jsCodeFile.toPath());
            jsCode = new String(bytes, "UTF-8");
        } catch (IOException e) {
            Logger.error("Failed to read file " + jsCodeFile, e);
            return null;
        }

        return new JsKioProblem(jsCode, className, settings, null); //TODO add external checker
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
