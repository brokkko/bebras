package models.data.features;

import models.Contest;
import models.ContestResult;
import models.User;
import models.data.Feature;
import play.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.05.13
 * Time: 22:39
 */
public class ContestResultFeatures {

    private User previousUser = null;
    private ContestResult contestResult = null;

    private final Contest contest;
    private final String contestName;

    public ContestResultFeatures(Contest contest) {
        this.contest = contest;
        this.contestName = contest.getId();
    }

    private ContestResult getContestResult(User user) {
        if (user != previousUser) {
            contestResult = contest.evaluateUserResults(user);
            previousUser = user;
            Logger.info("getting info for user " + user.getLogin());
        }

        return contestResult;
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
}
