package models.newproblems.bebras;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 23:17
 */
public class BebrasDynamicProblem implements Problem {

    public static final int MAX_ANSWER_LENGTH = 10;

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
    private String country; /*two letters country code according to ISO*/
    private String problemScript;
    private String imagesHtml;
    private String statement;
    private String explanation;
    private String informatics;
    private String dependencies;
    private String correctAnswer;
    private String taskStatementCssClass;
    private int height;

    public BebrasDynamicProblem() {
    }

    private String randomString(int len) {
        char[] result = new char[len];

        for (int i = 0; i < len; i++)
            result[i] = UNIQUE_ID_CHARS[rnd.nextInt(UNIQUE_ID_CHARS.length)];

        return new String(result);
    }

    @Override
    public Html format(String index, boolean showSolutions, Info settings, long randSeed) {
        int scores = 0;
        if (settings != null) {
            Object oScores = settings.get("r");
            if (oScores != null && oScores instanceof Integer)
                scores = (Integer) oScores;
        }

        //extract images from imagesHtml
        Pattern imgPattern = Pattern.compile("<img.*?src\\s*=\\s*['\"](.*?)['\"].*?title\\s*=\\s*['\"](.*?)['\"]"); //TODO or title="", src=""
        Matcher matcher = imgPattern.matcher(imagesHtml);
        Map<String, String> imgLinks = new HashMap<>();
        while (matcher.find())
            imgLinks.put(matcher.group(2), matcher.group(1));

        String uniqueId = randomString(20);

        return views.html.bebras.bebras_dyn_problem.render(
                index, scores, showSolutions,
                title, country, BebrasProblem.COUNTRY_TO_NAME.get(country),
                statement, problemScriptToRender(), imgLinks, explanation, informatics, height, uniqueId, correctAnswer,
                taskStatementCssClass
        );
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public Html formatEditor() {
        return views.html.bebras.bebras_dyn_editor.render(
                title, country, statement, problemScript, imagesHtml, explanation, informatics, correctAnswer, dependencies, taskStatementCssClass, height
        );
    }

    private String problemScriptToRender() {
        if (problemScript == null || problemScript.trim().isEmpty())
            return "function Task(container, images) {\n" +
                    "    this.enabled = true;\n" +
                    "    this.initCallback = null;\n" +
                    "}\n" +
                    "\n" +
                    "Task.reset = function () {\n" +
                    "};\n" +
                    "\n" +
                    "Task.isEnabled = function () {\n" +
                    "    return this.enabled;\n" +
                    "};\n" +
                    "\n" +
                    "Task.setEnabled = function (state) {\n" +
                    "    this.enabled = state;\n" +
                    "};\n" +
                    "\n" +
                    "Task.setInitCallback = function (_initCallback) {\n" +
                    "    this.initCallback = _initCallback;\n" +
                    "\n" +
                    "    if (this.initCallback)\n" +
                    "        this.initCallback();\n" +
                    "};\n" +
                    "\n" +
                    "Task.getSolution = function () {\n" +
                    "    return '';\n" +
                    "};\n" +
                    "\n" +
                    "Task.loadSolution = function (solution) {\n" +
                    "\n" +
                    "};\n" +
                    "\n" +
                    "Task.getAnswer = function () {\n" +
                    "    return 2;\n" +
                    "};\n" +
                    "\n" +
                    "return Task;";
        else
            return problemScript;
    }

    @Override
    public void updateProblem(RawForm form) {
        title = form.get("title");
        country = form.get("country");
        statement = form.get("statement");
        problemScript = form.get("script");
        imagesHtml = form.get("images");
        explanation = form.get("explanation");
        informatics = form.get("informatics");
        correctAnswer = form.get("correct_answer");
        dependencies = form.get("dependencies");
        taskStatementCssClass = form.get("task_statement_css_class");
        try {
            height = Integer.parseInt(form.get("height"));
        } catch (NumberFormatException e) {
            height = 0;
        }
    }

    @Override
    public String answerToString(Info answer, long randSeed) {
        if (answer == null)
            return "-";

        Info checked = check(answer, randSeed);
        int result = (Integer)checked.get("result");
        if (result == 0)
            return ".";

        String text = result < 0 ? "w" : "R";

        if (showFullAnswer()) {
            String userAnswer = (String)answer.get("s");
            if (userAnswer == null || userAnswer.isEmpty())
                return "/";
            String dispUserAnswer = userAnswer.length() > MAX_ANSWER_LENGTH ? userAnswer.substring(0, MAX_ANSWER_LENGTH) + "~" : userAnswer;

            return "[" + text + dispUserAnswer + ']';
        }

        return text;
    }

    @Override
    public String answerString() {
        if (showFullAnswer()) {
            if (correctAnswer.length() == 1)
                return correctAnswer;
            else
                return '[' + correctAnswer + ']';
        } else
            return "R";
    }

    @Override
    public Info check(Info answer, long randSeed) {
        Info result = new Info();

        Integer res = (Integer) answer.get("r");

        if (res == null || res < 0) {
            result.put("result", 0);
            result.put("answer", ".");
        } else if (res == 2) {
            String solution = (String) answer.get("s");

            if (solution == null || solution.isEmpty()) {
                result.put("result", 0);
                result.put("answer", ".");
            } else {
                boolean correct = testAnswerCorrectnessByServerChecker(solution);
                result.put("result", correct ? 1 : -1);
                result.put("answer", correct ? "R" : "w");
            }
        } else {
            result.put("result", res == 0 ? -1 : 1);
            result.put("answer", res == 0 ? "w" : "R");
        }

        return result;
    }

    private boolean testAnswerCorrectnessByServerChecker(String solution) {
        Pattern p = Pattern.compile("\\{{3}(\\w+)}{3}(.*)");
        String ca = correctAnswer;

        boolean doLowerCase = false;
        boolean doUpperCase = false;
        boolean doRemoveSpaces = false;
        while (true) {
            Matcher m = p.matcher(ca);
            if (!m.matches())
                break;
            String command = m.group(1).toLowerCase();
            ca = m.group(2);

            switch (command) {
                case "lower":
                    doLowerCase = true;
                    break;
                case "upper":
                    doUpperCase = true;
                    break;
                case "nospaces":
                    doRemoveSpaces = true;
                    break;
            }
        }

        if (doLowerCase)
            solution = solution.toLowerCase();
        if (doUpperCase)
            solution = solution.toUpperCase();
        if (doRemoveSpaces)
            solution = solution.replaceAll("\\s", "");

        solution = solution.trim();

        String[] split = correctAnswer.split("\\{{3}OR}{3}");
        for (String s : split)
            if (solution.equals(s))
                return true;
        return false;
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return new InfoPattern(
                "r", new BasicSerializationType<>(int.class), "result", //-1 no ans, 0 - wrong, 1 - true
                "s", new BasicSerializationType<>(String.class), "solution"
        );
    }

    @Override
    public InfoPattern getCheckerPattern() {
        return new InfoPattern(
                "result", new BasicSerializationType<>(int.class), "Результат",
                "answer", new BasicSerializationType<>(String.class), "Ответ"
        );
    }

    @Override
    public String getType() {
        return "bebras-dyn";
    }

    @Override
    public Widget getWidget(boolean editor) {
        ListWidget w = new ListWidget(
                new ResourceLink("bebras.problem.css"),
                new ResourceLink("bebras-dyn.problem.js"),
                new ResourceLink("polyfill.min.js")
        );

        if (editor)
            w = w.add(new ResourceLink("bebras.edit.problem.js"));

        if (!"nothing".equals(dependencies))
            for (String dependency : dependencies.split(","))
                w = w.add(new ResourceLink(dependency.trim() + ".js"));

        return w;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
        serializer.write("country", country);
        serializer.write("statement", statement);
        serializer.write("script", problemScript);
        serializer.write("images", imagesHtml);
        serializer.write("explanation", explanation);
        serializer.write("informatics", informatics);
        serializer.write("height", height);
        serializer.write("correct_answer", correctAnswer);
        serializer.write("dependencies", dependencies);
        serializer.write("task_statement_css_class", taskStatementCssClass);
    }

    @Override
    public void update(Deserializer deserializer) {
        title = deserializer.readString("title", "");
        country = deserializer.readString("country", "").toUpperCase();
        statement = deserializer.readString("statement", "");
        problemScript = deserializer.readString("script", "");
        imagesHtml = deserializer.readString("images", "");
        explanation = deserializer.readString("explanation", "");
        informatics = deserializer.readString("informatics", "");
        height = deserializer.readInt("height", 0);
        correctAnswer = deserializer.readString("correct_answer", "");
        dependencies = deserializer.readString("dependencies", "kinetic,ddlib");
        taskStatementCssClass = deserializer.readString("task_statement_css_class", "big-font");
    }

    private boolean showFullAnswer() {
        return correctAnswer != null && !correctAnswer.isEmpty() && correctAnswer.length() <= MAX_ANSWER_LENGTH;
    }
}
