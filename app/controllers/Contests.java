package controllers;

import controllers.actions.Authenticated;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import controllers.package$;
import models.Contest;
import models.User;
import models.problems.Answer;
import models.problems.Problem;
import play.mvc.Controller;
import play.mvc.Result;
import views.ResourceLink;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 03.05.13
 * Time: 1:17
 */
@LoadEvent
@Authenticated
@LoadContest
public class Contests extends Controller {

    public static Result startContest(String eventId, String contestId) {
        return ok(views.html.start_contest_confirmation.render());
    }

    public static Result contest(String eventId, String contestId) {
        User user = User.current();

        List<List<Problem>> pagedUserProblems = Contest.current().getPagedUserProblems(user.getId());

        List<ResourceLink> cssLinksList = getCssLinks(pagedUserProblems);
        List<ResourceLink> jsLinksList = getJsLinks(pagedUserProblems);

        List<Answer> answersForContest = user.getAnswersForContest(Contest.current());

        Map<Problem, Answer> problem2answer = new HashMap<>();
        Map<Problem, Integer> problem2index = new HashMap<>();
        int index = 0;
        for (List<Problem> page : pagedUserProblems)
            for (Problem problem : page) {
                problem2answer.put(problem, answersForContest.get(index));
                problem2index.put(problem, index);

                index++;
            }

        return ok(views.html.contest.render(pagedUserProblems, problem2index, problem2answer, cssLinksList, jsLinksList));
    }

    private static List<ResourceLink> getJsLinks(List<List<Problem>> pagedUserProblems) {
        Set<String> jsLinks = new HashSet<>();

        for (List<Problem> page : pagedUserProblems)
            for (Problem problem : page)
                jsLinks.add(problem.getJsLink());

        List<ResourceLink> jsLinksList = new ArrayList<>();
        for (String jsLink : jsLinks)
            jsLinksList.add(new ResourceLink(jsLink, "js"));
        return jsLinksList;
    }

    private static List<ResourceLink> getCssLinks(List<List<Problem>> pagedUserProblems) {
        Set<String> cssLinks = new HashSet<>();

        for (List<Problem> page : pagedUserProblems)
            for (Problem problem : page)
                cssLinks.add(problem.getCssLink());

        List<ResourceLink> cssLinksList = new ArrayList<>();
        for (String cssLink : cssLinks)
            cssLinksList.add(new ResourceLink(cssLink, "css"));
        return cssLinksList;
    }

}
