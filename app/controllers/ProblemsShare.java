package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Contest;
import models.Event;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.ProblemInfo;
import models.newproblems.ProblemLink;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.util.*;

import static controllers.Contests.getProblemsWidgets;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 07.09.13
 * Time: 20:42
 */
@LoadEvent
@DcesController
@Authenticated(redirectToLogin = false)
public class ProblemsShare extends Controller {
    public static Result viewPrintProblem(String eventId, String pidAsString, boolean answers) {
        if (viewUnavailable())
            return forbidden();

        ObjectId pid;
        try {
            pid = new ObjectId(pidAsString);
        } catch (IllegalArgumentException ignored) {
            return notFound(error.render("Не удается найти задачу", new String[0]));
        }

        ProblemInfo pi = ProblemInfo.get(pid);
        if (pi == null)
            return notFound(error.render("Не удается найти задачу", new String[0]));

        return ok(views.html.problem_view_print.render(pid, pi.getProblem(), answers));
    }

    public static Result allContestProblems(String eventId, String contestId, boolean showAnswers) {
        if (viewUnavailable())
            return forbidden();

        User user = User.current();

        Contest contest = Contest.current();

        List<ConfiguredProblem> allUserProblems = contest.getAllPossibleProblems();
        List<List<ConfiguredProblem>> pagedUserProblems = new ArrayList<>(1);
        pagedUserProblems.add(allUserProblems);

        Map<ConfiguredProblem, String> problem2title = new HashMap<>();
        for (ConfiguredProblem problem : allUserProblems)
            problem2title.put(problem, problem.getName());

        return ok(views.html.contest_print.render(
                showAnswers,
                pagedUserProblems,
                problem2title,
                getProblemsWidgets(pagedUserProblems),
                user == null ? 0 : user.getContestRandSeed(contestId)
        ));
    }


    public static Result printFolder(String eventId, String folder, boolean subfolders, boolean showAnswers) {
        if (viewUnavailable())
            return forbidden();

        ProblemLink link = new ProblemLink(folder);

        List<List<ConfiguredProblem>> pagedProblems = new LinkedList<>(); //one page for one folder
        listProblems(link, subfolders, pagedProblems);

        Map<ConfiguredProblem, String> problem2title = new HashMap<>();
        for (List<ConfiguredProblem> problemsInPage : pagedProblems)
            for (ConfiguredProblem problem : problemsInPage)
                problem2title.put(problem, problem.getName().substring(link.getLink().length() + 1)); //1 for slash /

        return ok(contest_print.render(showAnswers, pagedProblems, problem2title, Contests.getProblemsWidgets(pagedProblems), 0L));
    }

    private static void listProblems(ProblemLink link, boolean recursively, List<List<ConfiguredProblem>> pagedProblems) {
        List<ConfiguredProblem> problems = new LinkedList<>();
        List<ProblemLink> links = link.listProblems();

        for (ProblemLink problemLink : links) {
            ProblemInfo info = ProblemInfo.get(problemLink.getProblemId());
            if (info == null) //broken link
                continue;

            problems.add(new ConfiguredProblem(
                    problemLink.getProblemId(),
                    info.getProblem(),
                    problemLink.getLink(),
                    null
            ));
        }

        pagedProblems.add(problems);

        if (recursively) //may be optimized with one query
            for (ProblemLink folderLink : link.listFolders())
                listProblems(folderLink, true, pagedProblems);
    }

    private static boolean viewUnavailable() {
        Event event = Event.current();
        if (Boolean.TRUE.equals(event.getExtraField("tasks-open")))
            return false;

        User user = User.current();
        return user == null || !user.hasEventAdminRight();
    }
}
