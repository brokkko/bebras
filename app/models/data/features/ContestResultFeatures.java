package models.data.features;

import controllers.EventAdministration;
import models.Contest;
import models.ContestResult;
import models.Submission;
import models.User;
import models.data.CsvDataWriter;
import models.data.Feature;
import play.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.05.13
 * Time: 22:39
 */
public class ContestResultFeatures {

    private User previousUser = null;
    private ContestResult contestResult = null;
    private List<Submission> submissions = null;
    private List<String> possibleProblems = null;

    private final Contest contest;
    private final String contestName;

    public ContestResultFeatures(Contest contest) {
        this.contest = contest;
        this.contestName = contest.getId();
        this.possibleProblems = contest.getAllPossibleProblems();
    }

    private void ensureUser(User user) {
        if (user == previousUser)
            return;

        submissions = user.getSubmissionsForContest(contest);
        contestResult = contest.evaluateUserResults(user, submissions);

        previousUser = user;
        Logger.info("getting info for user " + user.getLogin());
    }

    private ContestResult getContestResult(User user) {
        ensureUser(user);

        return contestResult;
    }

    private List<Submission> getSubmissions(User user) {
        ensureUser(user);

        return submissions;
    }

    public Feature<User> getNumRightFeature() {
        return new NumRightFeature();
    }

    public Feature<User> getNumWrongFeature() {
        return new NumWrongFeature();
    }

    public Feature<User> getNumSkippedFeature() {
        return new NumSkippedFeature();
    }

    public Feature<User> getScoresFeature() {
        return new ScoresFeature();
    }

    public Feature<User> getLastSubmissionTimeFeature() {
        return new LastSubmissionTimeFeature();
    }

    public void appendProblemsFeatures(CsvDataWriter<User> dataWriter) {
        for (final String pid : possibleProblems) {
            dataWriter.addFeature(new Feature<User>() {
                @Override
                public String name() {
                    return pid;
                }

                @Override
                public String eval(User object) {
                    for (Submission submission : submissions)
                        if (submission != null && submission.getProblemId().equals('/' + pid)) //TODO WTF '/' + ?
                            return EventAdministration.submissionToAnswer(submission);
                    return "-";
                }
            });
        }
    }

    public void appendOrderedProblemsFeatures(CsvDataWriter<User> dataWriter) {
        int problemsCount = contest.getProblemsCount();
        for (int i = 0; i < problemsCount; i++) {
            final int finalI = i;

            dataWriter.addFeature(new Feature<User>() {
                @Override
                public String name() {
                    return contestName + ".id-" + (finalI + 1);
                }

                @Override
                public String eval(User user) {
                    return contest.getConfiguredUserProblems(user).get(finalI).getLink();
                }
            });

            dataWriter.addFeature(new Feature<User>() {
                @Override
                public String name() {
                    return contestName + ".ans-" + (finalI + 1);
                }

                @Override
                public String eval(User user) {
                    Submission submission = getSubmissions(user).get(finalI);
                    return EventAdministration.submissionToAnswer(submission);
                }
            });
        }
    }

    private class NumRightFeature implements Feature<User> {

        @Override
        public String name() {
            return contestName + ".num_right";
        }

        @Override
        public String eval(User user) {
            return "" + getContestResult(user).getR();
        }

    }

    private class NumWrongFeature implements Feature<User> {

        @Override
        public String name() {
            return contestName + ".num_wrong";
        }

        @Override
        public String eval(User user) {
            return "" + getContestResult(user).getW();
        }

    }

    private class NumSkippedFeature implements Feature<User> {

        @Override
        public String name() {
            return contestName + ".num_skipped";
        }

        @Override
        public String eval(User user) {
            return "" + getContestResult(user).getN();
        }

    }

    private class ScoresFeature implements Feature<User> {

        @Override
        public String name() {
            return contestName + ".scores";
        }

        @Override
        public String eval(User user) {
            return "" + getContestResult(user).getScores();
        }

    }

    private class LastSubmissionTimeFeature implements Feature<User> {

        @Override
        public String name() {
            return contestName + ".last_submission";
        }

        @Override
        public String eval(User user) {
            List<Submission> submissions = getSubmissions(user);
            long lastSubmission = 0;
            for (Submission submission : submissions) {
                if (submission == null)
                    continue;
                long localTime = submission.getLocalTime();
                if (localTime > lastSubmission)
                    lastSubmission = localTime;
            }

            int seconds = (int) Math.round(lastSubmission / 1000.0);

            int minutes = seconds / 60;
            seconds = seconds % 60;
            return num2str2digits(minutes) + ":" + num2str2digits(seconds);
        }

        private String num2str2digits(int num) {
            String result = num + "";
            while (result.length() < 2)
                    result = "0" + result;

            return result;
        }
    }
}
