package models.newproblems.bbtc;

import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import play.api.templates.Html;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.07.13
 * Time: 18:25
 */
public class BBTCProblem implements Problem {

    private static final String FIELD_QUESTION = "q";
    private static final String FIELD_ANSWERS = "a";
    private static final String FIELD_RIGHT_ANSWER = "r";
    private static final String FIELD_SCORES_RIGHT = "sc_r";
    private static final String FIELD_SCORES_WRONG = "sc_w";

    private String question = "";
    private List<String> answers;
    private int rightAnswer;

    private int scoresRight;
    private int scoresWrong;

    public BBTCProblem() {
    }

    public BBTCProblem(String question, List<String> answers, String rightAnswer, int scoresRight, int scoresWrong) {
        this.question = question;
        this.answers = answers;
        this.rightAnswer = stringAnswer2number(rightAnswer);

        this.scoresRight = scoresRight;
        this.scoresWrong = scoresWrong;
    }

    @Override
    public Html format(String index, boolean showSolutions, Info settings, long randSeed) {
        return views.html.bbtc.bbtc_problem.render(index, showSolutions, question, answers, rightAnswer);
    }

    @Override
    public boolean editable() {
        return false;
    }

    @Override
    public Html formatEditor() {
        return null;
    }

    @Override
    public void updateProblem(RawForm form) {
        //TODO implement
    }

    @Override
    public String answerToString(Info answer, long randSeed) {
        if (answer == null)
            return "-";

        Integer ansInt = (Integer) answer.get("a");
        if (ansInt < 0)
            return ".";

        Info check = check(answer, randSeed);
        int res = (Integer) check.get("result");

        if (res < 0)
            return (char)(ansInt + 'a') + "";
        else if (res == 0)
            return ".";
        else
            return (char)(ansInt + 'A') + "";
    }

    @Override
    public String answerString() {
        return (char)(rightAnswer + 'A') + "";
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return new InfoPattern("a", new BasicSerializationType<>(int.class), "answer");
    }

    @Override
    public Info check(Info answer, long randSeed) {
        Info result = new Info();

        Integer ans = (Integer) answer.get("a");

        if (ans == null) {
            result.put("result", 0);
            result.put("answer", ".");
        } else if (ans < 0) {
            result.put("result", 0);
            result.put("answer", ".");
        } else {
            result.put("result", ans == rightAnswer ? scoresRight : scoresWrong);
            result.put("answer", numberAnswer2string(ans));
        }

        return result;
    }

    @Override
    public String getType() {
        return "bbtc";
    }

    @Override
    public Widget getWidget(boolean editor) {
        return new ListWidget(
                new ResourceLink("bbtc.problem.css"),
                new ResourceLink("bbtc.problem.js")
        );
    }

    public static int stringAnswer2number(String answer) {
        return answer.charAt(0) - 'A';
    }

    public static String numberAnswer2string(int answer) {
        if (answer < 0)
            return "";
        return String.valueOf((char)('A' + answer));
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write(FIELD_QUESTION, question);
        SerializationTypesRegistry.list(String.class).write(serializer, FIELD_ANSWERS, answers);
        serializer.write(FIELD_RIGHT_ANSWER, rightAnswer);

        serializer.write(FIELD_SCORES_RIGHT, scoresRight);
        serializer.write(FIELD_SCORES_WRONG, scoresWrong);
    }

    @Override
    public void update(Deserializer deserializer) {
        question = deserializer.readString(FIELD_QUESTION);
        answers = SerializationTypesRegistry.list(String.class).read(deserializer, FIELD_ANSWERS);
        rightAnswer = deserializer.readInt(FIELD_RIGHT_ANSWER);

        scoresRight = deserializer.readInt(FIELD_SCORES_RIGHT);
        scoresWrong = deserializer.readInt(FIELD_SCORES_WRONG);
    }
}
