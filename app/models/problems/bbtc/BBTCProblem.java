package models.problems.bbtc;

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
    public Html format(boolean showSolutions) {
        return views.html.bbtc.bbtc_problem.render(showSolutions, question, Arrays.asList(answers));
    }

    @Override
    public String getJsLink() {
        return "bbtc.problems";
    }

    @Override
    public String getCssLink() {
        return "bbtc.problems";
    }

    @Override
    public void check(Deserializer submission, Serializer resultsReceiver) {
        String answer = submission.getString("a");
        if (answer == null)
            return;

        if (answer.isEmpty())
            resultsReceiver.write("result", 0);
        else {
            int userAnswer = stringAnswer2number(answer);
            resultsReceiver.write("result", userAnswer == correctAnswer ? -1 : 1);
        }
    }

    private static int stringAnswer2number(String answer) {
        return answer.charAt(0) - 'A';
    }
}