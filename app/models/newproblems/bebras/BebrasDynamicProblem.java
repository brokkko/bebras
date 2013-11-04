package models.newproblems.bebras;

import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import play.api.templates.Html;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 23:17
 */
public class BebrasDynamicProblem implements Problem {

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

    public BebrasDynamicProblem() {
    }

    private String randomString(int len) {
        char[] result = new char[len];

        for (int i = 0; i < len; i++)
            result[i] = UNIQUE_ID_CHARS[rnd.nextInt(UNIQUE_ID_CHARS.length)];

        return new String(result);
    }

    @Override
    public Html format(int index, boolean showSolutions, Info settings) {
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
                statement, problemScript, imgLinks, explanation, uniqueId
        );
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public Html formatEditor() {
        return views.html.bebras.bebras_dyn_editor.render(
                title, country, statement, problemScript, imagesHtml, explanation
        );
    }

    @Override
    public void updateProblem(RawForm form) {
        title = form.get("title");
        country = form.get("country");
        statement = form.get("statement");
        problemScript = form.get("script");
        imagesHtml = form.get("images");
        explanation = form.get("explanation");
    }

    @Override
    public String answerToString(Info answer) {
        if (answer == null)
            return "-";

        Integer res = (Integer) answer.get("r");
        if (res == null || res < 0)
            return ".";
        else if (res == 0)
            return "w";
        else
            return "R";
    }

    @Override
    public Info check(Info answer) {
        Info result = new Info();

        Integer res = (Integer) answer.get("r");

        if (res == null || res < 0) {
            result.put("result", 0);
            result.put("answer", ".");
        } else {
            result.put("result", res == 0 ? -1 : 1);
            result.put("answer", res == 0 ? "w" : "R");
        }

        return result;
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return new InfoPattern(
                "r", new BasicSerializationType<>(int.class), "result", //-1 no ans, 0 - wrong, 1 - true
                "s", new BasicSerializationType<>(String.class), "solution"
        );
    }

    @Override
    public String getType() {
        return "bebras-dyn";
    }

    @Override
    public Widget getWidget(boolean editor) {
        if (editor)
            return new ListWidget(
                    new ResourceLink("bebras.problem.css"),
                    new ResourceLink("bebras.edit.problem.js"),
                    new ResourceLink("kinetic.js"),
                    new ResourceLink("ddlib.js"),
                    new ResourceLink("bebras-dyn.problem.js")
            );
        else
            return new ListWidget(
                    new ResourceLink("bebras.problem.css"),
                    new ResourceLink("bebras-dyn.problem.js"),
                    new ResourceLink("kinetic.js"),
                    new ResourceLink("ddlib.js")
            );
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
        serializer.write("country", country);
        serializer.write("statement", statement);
        serializer.write("script", problemScript);
        serializer.write("images", imagesHtml);
        serializer.write("explanation", explanation);
    }

    @Override
    public void update(Deserializer deserializer) {
        title = deserializer.readString("title", "");
        country = deserializer.readString("country", "").toUpperCase();
        statement = deserializer.readString("statement", "");
        problemScript = deserializer.readString("script", "");
        imagesHtml = deserializer.readString("images", "");
        explanation = deserializer.readString("explanation", "");
    }
}
