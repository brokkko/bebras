package models.problems.bbtc;

import models.problems.Problem;
import models.serialization.Deserializer;
import models.serialization.Serializer;
import play.api.templates.Html;

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
    public Html formatStatement() {
        return null; // bbtc.render(question, answers, -1);
    }

    @Override
    public Html formatStatementWithSolution() {
        return null; //bbtc.render(question, answers, correctAnswer);
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

        int userAnswer = stringAnswer2number(answer);

        resultsReceiver.write("result", userAnswer == correctAnswer ? 0 : 1);
    }

    private static int stringAnswer2number(String answer) {
        return answer.charAt(0) - 'A';
    }
}