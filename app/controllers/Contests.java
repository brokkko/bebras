package controllers;

import controllers.actions.Authenticated;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import controllers.package$;
import models.Contest;
import models.User;
import models.problems.Problem;
import play.mvc.Controller;
import play.mvc.Result;
import views.ResourceLink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Set<String> cssLinks = new HashSet<>();
        Set<String> jsLinks = new HashSet<>();

        for (List<Problem> page : pagedUserProblems)
            for (Problem problem : page) {
                cssLinks.add(problem.getCssLink());
                jsLinks.add(problem.getJsLink());
            }
        
        List<ResourceLink> cssLinksList = new ArrayList<>();
        for (String cssLink : cssLinks)
            cssLinksList.add(new ResourceLink(cssLink, "css"));

        List<ResourceLink> jsLinksList = new ArrayList<>();
        for (String jsLink : jsLinks)
            jsLinksList.add(new ResourceLink(jsLink, "js"));

        return ok(views.html.contest.render(pagedUserProblems, cssLinksList, jsLinksList));
    }

}
