package models.checkers;

import models.problems.Problem;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 13:11
 */
public class ComparatorChecker extends Checker {

    public static final String SUBMITTED_ANSWER = "ans";
    public static final String RIGHT_ANSWER = "right_answer";
    public static final String RESULT = "res";

/*    @Override
    public void check(StoredObject submission, Problem problem, StoredObject resultsReceiver) {
        String userAnswer = submission.getString(SUBMITTED_ANSWER);

        if (userAnswer == null) {
            resultsReceiver.put(RESULT, 0);
            return;
        }

        String rightAnswer = problem.getString(RIGHT_ANSWER);

        boolean correct = rightAnswer.equals(userAnswer);

        resultsReceiver.put(RESULT, correct ? 1 : -1);
    }*/
}
