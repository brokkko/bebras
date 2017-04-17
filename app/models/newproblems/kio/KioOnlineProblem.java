package models.newproblems.kio;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import play.twirl.api.Html;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.io.IOException;
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
                title, statement, help, className, settings, dependencies
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

        result.fields().forEachRemaining(e -> {
            checkResult.put(e.getKey(), e.getValue());
        });

        return checkResult;
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return new InfoPattern(
                "sol", new BasicSerializationType<>(String.class), "solution",
                "res", new BasicSerializationType<>(String.class), "result"
        );
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

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
        serializer.write("statement", statement);
        serializer.write("help", help);
        serializer.write("className", className);
        serializer.write("settings", settings);
        serializer.write("dependencies", dependencies);
    }

    @Override
    public void update(Deserializer deserializer) {
        title = deserializer.readString("title", "");
        statement = deserializer.readString("statement", "");
        help = deserializer.readString("help", "");
        className = deserializer.readString("className", "");
        settings = deserializer.readString("settings", "");
        dependencies = deserializer.readString("dependencies", "");
    }
}
