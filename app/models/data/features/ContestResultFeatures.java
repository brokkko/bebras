//package models.data.features;
//
//import com.mongodb.BasicDBObject;
//import com.mongodb.DBCollection;
//import com.mongodb.DBCursor;
//import com.mongodb.DBObject;
//import controllers.EventAdministration;
//import models.*;
//import models.data.CsvDataWriter;
//import models.problems.ConfiguredProblem;
//import models.newserialization.MongoDeserializer;
//import models.results.Info;
//import play.Logger;
//
//import java.util.List;
//
///**
//* Created with IntelliJ IDEA.
//* User: ilya
//* Date: 23.05.13
//* Time: 22:39
//*/
//public class ContestResultFeatures {
//
//    private User previousUser = null;
//    private Info contestResult = null;
//    private List<Submission> submissions = null;
//    private List<String> possibleProblems = null;
//
//    private final Contest contest;
//    private final String contestName;
//
//    public ContestResultFeatures(Contest contest) {
//        this.contest = contest;
//        this.contestName = contest.getId();
//        this.possibleProblems = contest.getAllPossibleProblems();
//    }
//
//    private void ensureUser(User user) {
//        if (user == previousUser)
//            return;
//
//        submissions = user.getSubmissionsForContest(contest);
//        contestResult = user.getContestResults(contest);
//
//        previousUser = user;
//        Logger.info("getting info for user " + user.getLogin());
//    }
//
//    private Info getContestResult(User user) {
//        ensureUser(user);
//
//        return contestResult;
//    }
//
//    private List<Submission> getSubmissions(User user) {
//        ensureUser(user);
//
//        return submissions;
//    }
//
//    public Feature<User> getNumRightFeature() {
//        return new NumRightFeature();
//    }
//
//    public Feature<User> getNumWrongFeature() {
//        return new NumWrongFeature();
//    }
//
//    public Feature<User> getNumSkippedFeature() {
//        return new NumSkippedFeature();
//    }
//
//    public Feature<User> getScoresFeature() {
//        return new ScoresFeature();
//    }
//
//    public Feature<User> getLastSubmissionTimeFeature() {
//        return new LastSubmissionTimeFeature();
//    }
//
//    public Feature<User> getProblemsOrderFeature() {
//        return new ProblemsOrderFeature();
//    }
//
//    public Feature<User> getUserHistoryFeature() {
//        return new UserHistoryFeature();
//    }
//
//    public void appendProblemsFeatures(CsvDataWriter<User> dataWriter) {
//        for (final String pid : possibleProblems) {
//            dataWriter.addFeature(new Feature<User>() {
//                @Override
//                public String name() {
//                    return pid;
//                }
//
//                @Override
//                public String eval(User object) {
//                    for (Submission submission : submissions)
//                        if (submission != null && submission.getProblemId().equals('/' + pid)) //TODO WTF '/' + ?
//                            return EventAdministration.submissionToAnswer(submission);
//                    return "-";
//                }
//            });
//        }
//    }
//
//    private class NumRightFeature implements Feature<User> {
//
//        @Override
//        public String name() {
//            return contestName + ".num_right";
//        }
//
//        @Override
//        public String eval(User user) {
//            return "" + getContestResult(user).get("r");
//        }
//
//    }
//
//    private class NumWrongFeature implements Feature<User> {
//
//        @Override
//        public String name() {
//            return contestName + ".num_wrong";
//        }
//
//        @Override
//        public String eval(User user) {
//            return "" + getContestResult(user).get("w");
//        }
//
//    }
//
//    private class NumSkippedFeature implements Feature<User> {
//
//        @Override
//        public String name() {
//            return contestName + ".num_skipped";
//        }
//
//        @Override
//        public String eval(User user) {
//            return "" + getContestResult(user).get("n");
//        }
//
//    }
//
//    private class ScoresFeature implements Feature<User> {
//
//        @Override
//        public String name() {
//            return contestName + ".scores";
//        }
//
//        @Override
//        public String eval(User user) {
//            return "" + getContestResult(user).get("scores");
//        }
//
//    }
//
//    private class LastSubmissionTimeFeature implements Feature<User> {
//
//        @Override
//        public String name() {
//            return contestName + ".last_submission";
//        }
//
//        @Override
//        public String eval(User user) {
//            List<Submission> submissions = getSubmissions(user);
//            long lastSubmission = 0;
//            for (Submission submission : submissions) {
//                if (submission == null)
//                    continue;
//                long localTime = submission.getLocalTime();
//                if (localTime > lastSubmission)
//                    lastSubmission = localTime;
//            }
//
//            return displayTimeInMillis(lastSubmission);
//        }
//    }
//
//    private class ProblemsOrderFeature implements Feature<User> {
//
//        @Override
//        public String name() {
//            return contestName + ".problems_order";
//        }
//
//        @Override
//        public String eval(User user) {
//            StringBuilder buffer = new StringBuilder(); //TODO report ??? why buffer can be changed to String??
//
//            for (ConfiguredProblem problem : contest.getUserProblems(user))
//                buffer.append(problem.getLink().substring("/bbtc/?/".length())).append(' ');
//
//            String s = buffer.toString();
//            return s.substring(0, s.length() - 1);
//        }
//    }
//
//    private class UserHistoryFeature implements Feature<User> {
//
//        @Override
//        public String name() {
//            return contestName + ".history";
//        }
//
//        @Override
//        public String eval(User user) {
//            DBCollection usersCollection = contest.getCollection();
//
//            DBObject query = new BasicDBObject("u", user.getId());
//            DBObject sort = new BasicDBObject("lt", 1);
//
//            StringBuilder result = new StringBuilder();
//
//            try (
//                    DBCursor usersCursor = usersCollection.find(query).sort(sort)
//            ) {
//                long previousLocalTime = -1;
//                while (usersCursor.hasNext()) {
//                    Submission submission = new Submission(contest, new MongoDeserializer(usersCursor.next()));
//                    if (submission.getLocalTime() == previousLocalTime)
//                        continue;
//
//                    previousLocalTime = submission.getLocalTime();
//
//                    String pid = submission.getProblemId().substring("/bbtc/?/".length());
//                    result
//                            .append(displayTimeInMillis(previousLocalTime))
//                            .append('|')
//                            .append(pid)
//                            .append('|')
//                            .append(EventAdministration.submissionToAnswer(submission))
//                            .append(' ');
//                }
//            }
//
//            String s = result.toString();
//            return s.length() == 0 ? "" : s.substring(0, s.length() - 1);
//        }
//    }
//
//}
