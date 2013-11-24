package models.data.features;

import models.Contest;
import models.Submission;
import models.User;
import models.utils.Utils;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.ProblemInfo;
import models.results.Info;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 26.07.13
 * Time: 15:03
 */
public class ContestHistoryFeatures implements FeaturesSet<User> {

    private User user;

    @Override
    public void load(User user) throws Exception {
        this.user = user;
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) throws Exception {
        String[] contestAndFeature = featureName.split("\\.");

        if (contestAndFeature.length != 2)
            throw new IllegalArgumentException("Unparsable feature name: " + featureName);

        String contestId = contestAndFeature[0];
        String feature = contestAndFeature[1];

        Contest contest = user.getEvent().getContestById(contestId);

        if (contest == null)
            throw new IllegalArgumentException("Unknown contest " + contestId);

        switch (feature) {
            case "problems_order":
                return problemsOrder(contest);
            case "history":
                return history(contest);
            case "answers_all":
                return answersForAllProblems(contest);
            case "answers":
                return answers(contest);
            case "right_answers":
                return rightAnswers(contest);
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        this.user = null;
    }

    private Object answers(Contest contest) {
        throw new IllegalStateException("not yet implemented");
    }

    private Object answersForAllProblems(Contest contest) {
        List<ConfiguredProblem> allPossibleProblems = contest.getAllPossibleProblems();

        List<Submission> contestSubmissions = user.getSubmissionsForContest(contest);
        List<ConfiguredProblem> userProblems = contest.getUserProblems(user);

        Map<ObjectId, Integer> pid2index = new HashMap<>();
        for (int i = 0; i < userProblems.size(); i++)
            pid2index.put(userProblems.get(i).getProblemId(), i);

        StringBuilder result = new StringBuilder();
        for (ConfiguredProblem problem : allPossibleProblems) {
            ObjectId pid = problem.getProblemId();
            Integer index = pid2index.get(pid);
            if (index == null)
                result.append('-');
            else {
                Problem p = problem.getProblem();
                Submission submission = contestSubmissions.get(index);
                if (submission == null)
                    result.append(".");
                else {
                    Info answer = submission.getAnswer();
                    result.append(p.answerToString(answer, user.getContestRandSeed(contest.getId())));
                }
            }
        }

        return result.toString();
    }

    private Object rightAnswers(Contest contest) {
        List<ConfiguredProblem> allPossibleProblems = contest.getAllPossibleProblems();

        StringBuilder result = new StringBuilder();
        for (ConfiguredProblem problem : allPossibleProblems)
            result.append(problem.getProblem().answerString());

        return result.toString();
    }

    private Object history(Contest contest) {
        List<Submission> allSubmissions = user.getAllSubmissions(contest);

        allSubmissions = new ArrayList<>(allSubmissions);
        Collections.sort(allSubmissions, new Comparator<Submission>() {
            @Override
            public int compare(Submission s1, Submission s2) {
                long dif = s1.getLocalTime() - s2.getLocalTime();
                if (dif < 0)
                    return -1;
                else if (dif == 0)
                    return 0;
                else
                    return 1;
            }
        });

        StringBuilder result = new StringBuilder();
        for (Submission submission : allSubmissions) {
            ObjectId pid = submission.getProblemId();
            if (pid == null)
                continue;
            Info answer = submission.getAnswer();
            Problem problem = ProblemInfo.get(pid).getProblem();
            String strAns = problem == null ? "?" : problem.answerToString(answer, user.getContestRandSeed(contest.getId())); //TODO think what to do with absent problems
            long localTime = submission.getLocalTime();

            result
                    .append(Utils.millis2minAndSec(localTime))
                    .append('|')
                    .append(contest.getProblemName(pid))
                    .append('|')
                    .append(strAns)
                    .append(' ');
        }

        return result.toString();
    }

    private Object problemsOrder(Contest contest) {
        List<ConfiguredProblem> userProblems = contest.getUserProblems(user);
        StringBuilder result = new StringBuilder();
        for (ConfiguredProblem userProblem : userProblems)
            result.append(userProblem.getName()).append(", ");

        //remove last space
        if (result.length() > 0)
            result.setLength(result.length() - 2);

        return result.toString();
    }
}
