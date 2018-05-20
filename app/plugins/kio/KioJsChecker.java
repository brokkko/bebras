package plugins.kio;

import com.mongodb.BasicDBObject;
import controllers.worker.Worker;
import models.Contest;
import models.Event;
import models.Submission;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.kio.KioOnlineProblem;
import models.results.Info;
import play.Logger;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import ru.ipo.kio.js.JsKioProblem;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

//      http://localhost:9000/kio18/kiojschecker/contest/elasticity0/p
//      http://localhost:9000/kio18/kiojschecker/contest/elasticity1/p
//      http://localhost:9000/kio18/kiojschecker/contest/elasticity2/p

//      http://localhost:9000/kio18/kiojschecker/contest/hexagons0/p
//      http://localhost:9000/kio18/kiojschecker/contest/hexagons1/p
//      http://localhost:9000/kio18/kiojschecker/contest/hexagons2/p

//      http://localhost:9000/kio18/kiojschecker/contest/lamps0/p
//      http://localhost:9000/kio18/kiojschecker/contest/lamps1/p
//      http://localhost:9000/kio18/kiojschecker/contest/lamps2/p

//      http://kio-nauka.ru/kio18/kiojschecker/contest/elasticity0/p
//      http://kio-nauka.ru/kio18/kiojschecker/contest/elasticity1/p
//      http://kio-nauka.ru/kio18/kiojschecker/contest/elasticity2/p

//      http://kio-nauka.ru/kio18/kiojschecker/contest/hexagons0/p
//      http://kio-nauka.ru/kio18/kiojschecker/contest/hexagons1/p
//      http://kio-nauka.ru/kio18/kiojschecker/contest/hexagons2/p

//      http://kio-nauka.ru/kio18/kiojschecker/contest/lamps0/p
//      http://kio-nauka.ru/kio18/kiojschecker/contest/lamps1/p
//      http://kio-nauka.ru/kio18/kiojschecker/contest/lamps2/p


public class KioJsChecker extends Plugin {

    @Override
    public void initPage() {
        //do nothing
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public F.Promise<Result> doGet(String action, String contestId) {
        if (!User.currentRole().hasEventAdminRight())
            return F.Promise.pure(Results.forbidden());

        Event event = Event.current();
        Logger.info("do get for kio js checker");

        switch (action) {
            case "go":
                Worker w = new Worker("test kio problems", "test all kio problems in this event");

                w.execute(() -> checkEvent(w, event, null));

                return F.Promise.pure(
                        Results.redirect(controllers.routes.EventAdministration.workersList(Event.currentId()))
                );
            case "contest":
                w = new Worker("test kio problems", "test kio problems in this event for contest: " + contestId);

                w.execute(() -> checkEvent(w, event, contestId));

                return F.Promise.pure(
                        Results.redirect(controllers.routes.EventAdministration.workersList(Event.currentId()))
                );
            case "clear":
                if (contestId.isEmpty())
                    for (Contest contest : Event.current().getContests())
                        clearResultsForContest(contest);
                else {
                    Contest contest = Event.current().getContestById(contestId);
                    if (contest == null)
                        return F.Promise.pure(Results.notFound("contest not found"));
                    clearResultsForContest(contest);
                }
                return F.Promise.pure(Results.redirect(controllers.routes.EventAdministration.workersList(Event.currentId())));
        }

        return F.Promise.pure(Results.notFound("action not found"));
    }

    private void clearResultsForContest(Contest contest) {
        contest.getCollection().update(
                new BasicDBObject(),
                new BasicDBObject("$unset", new BasicDBObject(Submission.CHECK_FIELD, 1))
        );
    }

    private void checkEvent(Worker w, Event event, String contestId) {
        User.UsersEnumeration ue = User.listUsers(new BasicDBObject(User.FIELD_EVENT, event.getId()));
        int cnt = 0;
        while (ue.hasMoreElements()) {
            User user = ue.nextElement();
            checkUser(w, event, user, contestId);

            cnt++;
//            if (cnt % 1 == 0)
            w.logInfo("users processed: " + cnt);
        }
    }

    /**
     * @param w
     * @param event
     * @param user
     * @param contestId may be null to check all contests
     */
    private void checkUser(Worker w, Event event, User user, String contestId) {
        List<Contest> contestsAvailableForUser = event.getContestsAvailableForUser(user);
        for (Contest contest : contestsAvailableForUser)
            if (contestId == null || contest.getId().equals(contestId))
                checkContest(w, event, contest, user);
    }

    private void checkContest(Worker w, Event event, Contest contest, User user) {
        List<ConfiguredProblem> userProblems = contest.getUserProblems(user);
        List<List<Submission>> allSubmissions = user.getSubmissionsListsForProblems(contest);

        for (int i = 0; i < userProblems.size(); i++) {
            ConfiguredProblem cp = userProblems.get(i);
            Problem problem = cp.getProblem();
            if (problem instanceof KioOnlineProblem) {
                List<Submission> submissions = allSubmissions.get(i);
                checkProblem(w, event, contest, user, (KioOnlineProblem) problem, submissions);
            }
        }
    }

    private void checkProblem(Worker w, Event event, Contest contest, User user, KioOnlineProblem problem, List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty())
            return;

        JsKioProblem jsKioProblem = problem.getJsKioProblem();
        if (jsKioProblem == null)
            throw new RuntimeException("Failed to get kio problem. Dependencies: " + problem.getDependencies());

        /*for (Submission submission : submissions) {
            try {
                checkSubmission(w, user, problem, jsKioProblem, submission);
            } catch (Exception e) {
                w.logError("error while processing a submission #" + submission.getId(), e);
                submission.setExternalCheckResult(null);
            }
        }*/

        Submission submission = submissions.get(submissions.size() - 1);

        try {
            checkSubmission(w, user, problem, jsKioProblem, submission);
        } catch (Exception e) {
            w.logError("error while processing a submission #" + submission.getId(), e);
            submission.setExternalCheckResult(null);
        }
    }

    private void checkSubmission(Worker w, User user, KioOnlineProblem problem, JsKioProblem jsKioProblem, Submission submission) throws IOException {
        if (submission.getExternalCheckResult() != null)
            return;

        Info answer = submission.getAnswer();
        String solutionJSON = (String) answer.get("sol");
        String submittedResultJSON = (String) answer.get("res");

        String resultJSON = jsKioProblem.check(solutionJSON);

        if (!Objects.equals(submittedResultJSON, resultJSON))
            w.logInfo(String.format(
                    "Recheck gives a different result:%n  solution:%n  user: %s%n  %s%n  user check:%n  %s%n  auto check:%n  %s",
                    user.getLogin(),
                    solutionJSON,
                    submittedResultJSON,
                    resultJSON
            ));

        if (resultJSON != null) {
            submission.setExternalCheckResult(
                    problem.check(resultJSON)
            );
        } else
            submission.setExternalCheckResult(new Info());
    }
}