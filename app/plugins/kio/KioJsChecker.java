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
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import ru.ipo.kio.js.JsKioProblem;

import java.util.List;

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
    public F.Promise<Result> doGet(String action, String params) {
        Worker w = new Worker("test kio problems", "test all kio problems in this event");

        w.execute(() -> checkEvent(w, Event.current()));

        return F.Promise.pure(
                Results.redirect(controllers.routes.EventAdministration.workersList(Event.currentId()))
        );
    }

    private void checkEvent(Worker w, Event event) {
        User.UsersEnumeration ue = User.listUsers(new BasicDBObject(User.FIELD_EVENT, event.getId()));
        while (ue.hasMoreElements()) {
            User user = ue.nextElement();
            checkUser(w, event, user);
        }
    }

    private void checkUser(Worker w, Event event, User user) {
        List<Contest> contestsAvailableForUser = event.getContestsAvailableForUser(user);
        for (Contest contest : contestsAvailableForUser)
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
        JsKioProblem jsKioProblem = problem.asJsKioProblem();
        if (jsKioProblem == null)
            return;
    }
}
