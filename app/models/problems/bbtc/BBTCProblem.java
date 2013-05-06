package models.problems.bbtc;

import models.problems.Answer;
import models.problems.Problem;
import models.serialization.Deserializer;
import models.serialization.Serializer;
import play.api.templates.Html;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.05.13
 * Time: 21:23
 */
public class BBTCProblem implements Problem {

    private String question;
    private String[] answers;
    private int correctAnswer;

    public BBTCProblem(String question, String[] answers, String correctAnswer) {
        this.question = question;
        this.answers = answers;
        this.correctAnswer = stringAnswer2number(correctAnswer);
    }

    @Override
    public Html format(int index, boolean showSolutions) {
        return views.html.bbtc.bbtc_problem.render(index, showSolutions, question, Arrays.asList(answers));
    }

    @Override
    public String getJsLink() {
        return "bbtc.problem";
    }

    @Override
    public String getCssLink() {
        return "bbtc.problem";
    }

    @Override
    public void check(Answer answer, Serializer resultsReceiver) {
        Integer ans = (Integer) answer.get("a");
        if (ans == null) {
            resultsReceiver.write("result", 0);
            return;
        }

        if (ans < 0)
            resultsReceiver.write("result", 0);
        else
            resultsReceiver.write("result", ans == correctAnswer ? 1 : -1);
    }

    @Override
    public String getType() {
        return "bbtc";
    }

    public static int stringAnswer2number(String answer) {
        return answer.charAt(0) - 'A';
    }

    public static String numberAnswer2string(int answer) {
        if (answer < 0)
            return "";
        return String.valueOf((char)('A' + answer));
    }
}