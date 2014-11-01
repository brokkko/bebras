package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.actions.*;
import models.*;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newserialization.JSONDeserializer;
import models.newserialization.JSONSerializer;
import models.newserialization.ListSerializer;
import models.newserialization.Serializer;
import models.results.Info;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

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
@DcesController
public class Contests extends Controller {

    @SuppressWarnings("UnusedParameters")
    public static Result startContest(String eventId, String contestId) {
        if (User.current() == null)
            autoRegisterUser();

        return ok(views.html.start_contest_confirmation.render());
    }

    private static void autoRegisterUser() {
        //TODO implement
    }

    public static Result contest(String eventId, String contestId, String displayType) {
        User user = User.current();

        Contest contest = Contest.current();

        Event event = Event.current();

        int status = user.getContestStatus(contest);
        if (status == 6 || status == 7)
            return forbidden();

        boolean printing = !displayType.equals("normal");

        List<List<ConfiguredProblem>> pagedUserProblems = contest.getPagedUserProblems(user);

//        List<Info> answersForContest = printing ? (List<Info>) Collections.emptyList() : user.getAnswersForContest(contest); //TODO report: removing of redundant cast leads to error
        List<Info> answersForContest;
        if (printing)
            answersForContest = Collections.nCopies(contest.getUserProblems(user).size(), null);
        else
            answersForContest = user.getAnswersForContest(contest);

        //fill json info with user answers
        JSONSerializer contestInfoSerializer = new JSONSerializer();
        ListSerializer problemsInfoSerializer = contestInfoSerializer.getListSerializer("problems");

        Map<ConfiguredProblem, Integer> problem2index = new HashMap<>();
        int index = 0;
        for (List<ConfiguredProblem> page : pagedUserProblems)
            for (ConfiguredProblem configuredProblem : page) {
                Problem problem = configuredProblem.getProblem();
                Serializer problemInfoSerializer = problemsInfoSerializer.getSerializer();
                Info answer = answersForContest.get(index);

                if (answer == null)
                    problemInfoSerializer.writeNull("ans");
                else
                    problem.getAnswerPattern().write(problemInfoSerializer, "ans", answer);
                problemInfoSerializer.write("type", problem.getType());

                problem2index.put(configuredProblem, index);

                index++;
            }

//        Logger.info("[4] " + (System.currentTimeMillis() - time)); time = System.currentTimeMillis();

        //return time that passed from the beginning
        Date contestStartTime = user.contestStartTime(contestId);
        if (contestStartTime == null) {
            contestStartTime = AuthenticatedAction.getRequestTime();
            if (!user.contestFinished(contest)) { //don't mark that user started contest if he can not start it. TODO check this condition in some other way
                user.setContestStartTime(contestId, contestStartTime);
                user.store();

                logUserStartedContest(eventId, contestId, user, contestStartTime);
            }
        }

        contestInfoSerializer.write("passed", AuthenticatedAction.getRequestTime().getTime() - contestStartTime.getTime());

        //return duration
        contestInfoSerializer.write("duration", contest.isUnlimitedTime() ? 0 : contest.getDurationInMs());
        //return weather user already finished
        contestInfoSerializer.write("finished", user.userParticipatedAndFinished(contest));
        //write user id (needed to distinguish data in local storage for different users
        contestInfoSerializer.write("storage_id", eventId + "-" + contestId + "-" + user.getLogin());
        //write urls
        contestInfoSerializer.write("submit_url", routes.Contests.submit(eventId, contestId).toString());
        contestInfoSerializer.write("stop_url", routes.Contests.stop(eventId, contestId).toString());
        //set status
        String textStatus = "wait";
        if (status == 1 || status == 5)
            textStatus = "going";
        if (status == 2)
            textStatus = "results";
        if (status == 3 && user.resultsAvailable(contest))
            textStatus = "results";

        if (printing) {
            String needStatus = displayType.equals("print") ? "going" : "results";
            if (!user.hasEventAdminRight() && !textStatus.equals("results") && !needStatus.equals(textStatus))
                return forbidden();
            textStatus = needStatus;
        }

        contestInfoSerializer.write("status", textStatus);

        if (printing) {
            Map<ConfiguredProblem, String> problem2title = new HashMap<>();
            for (Map.Entry<ConfiguredProblem, Integer> problemIndexEntry : problem2index.entrySet())
                problem2title.put(problemIndexEntry.getKey(), "" + (1 + problemIndexEntry.getValue()));

            return ok(views.html.contest_print.render(
                 textStatus.equals("results"),
                 pagedUserProblems,
                 problem2title,
                 getProblemsWidgets(pagedUserProblems),
                 user.getContestRandSeed(contestId)
            ));
        } else
            return ok(views.html.contest.render(
                                                       textStatus,
                                                       pagedUserProblems,
                                                       problem2index,
                                                       contestInfoSerializer.getNode().toString(),
                                                       getProblemsWidgets(pagedUserProblems),
                                                       event.getExtraField("contests_no_header", false) == Boolean.FALSE,
                                                       event.getExtraField("contests_no_footer", false) == Boolean.FALSE,
                                                       event.getExtraField("contests_scrolling", false) == Boolean.TRUE,
                                                       event.getExtraField("contests_menu_to_right", false) == Boolean.FALSE,
                                                       event.getExtraField("contests_no_menu", false) == Boolean.FALSE,
                                                       event.getExtraField("contests_no_top_pages", false) == Boolean.FALSE,
                                                       event.getExtraField("contests_no_bottom_pages", false) == Boolean.FALSE,
                                                       event.getExtraField("contests_no_next_buttons", false) == Boolean.FALSE
            ));
    }

    private static void logUserStartedContest(String eventId, String contestId, User user, Date contestStartTime) {
        // do not log anonymous users
        if (user.getRole().equals(UserRole.ANON))
            return;

        Logger.info(String.format("Event %s user %s started contest %s at %tc",
                eventId, user.getId(), contestId, contestStartTime
        ));
    }

    public static Result allContestProblems(String eventId, String contestId, boolean showAnswers) {
        User user = User.current();
        if (!user.hasEventAdminRight())
            return forbidden();

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
                user.getContestRandSeed(contestId)
        ));
    }

    @SuppressWarnings("UnusedParameters")
    @BodyParser.Of(BodyParser.Json.class)
    public static Result submit(String eventId, String contestId) {
        Contest contest = Contest.current();

        JsonNode submissionJson = request().body().asJson();
        if (!(submissionJson instanceof ArrayNode))
            return badRequest();

        //get all submissions
        List<Submission> submissions = new ArrayList<>();
        for (JsonNode jsonNode : (ArrayNode) submissionJson) {
            if (!(jsonNode instanceof ObjectNode))
                return badRequest();

            JSONDeserializer deserializer = new JSONDeserializer((ObjectNode) jsonNode);
            Submission submission = new Submission(contest, deserializer);

            //skip submissions that are too late
            if (!contest.isUnlimitedTime() && submission.getLocalTime() > contest.getDurationInMs())
                continue;

            if (submission.isSystem())
                processSystemSubmission(submission);

            submissions.add(submission);
        }

        User user = User.current();
        user.invalidateContestResults(contestId);

        //store all submissions
        for (Submission submission : submissions)
            submission.serialize();

        return ok();
    }

    private static void processSystemSubmission(Submission submission) {
//        if ("page".equals(submission.getSystemField())) {
//            Logger.info("user moved to page " + submission.getSystemValue());
//        }
        //TODO do something
    }

    @SuppressWarnings("UnusedParameters")
    public static Result stop(String eventId, String contestId) {
        Contest contest = Contest.current();
        User user = User.current();

        if (!user.contestIsGoing(contest))
            return forbidden();

        user.setContestFinishTime(contestId, AuthenticatedAction.getRequestTime());
        user.store();

        return ok();
    }

    public static Result restart(String eventId, String contestId) {
        Contest contest = Contest.current();

        if (!contest.isAllowRestart() && !User.current().hasEventAdminRight())
            return forbidden();

        User user = User.current();

        if (!user.userParticipatedAndFinished(contest))
            return forbidden();

        user.setContestStartTime(contestId, null);
        user.setContestFinishTime(contestId, null);
        user.generateContestRandSeed(contestId);
        user.invalidateContestResults(contest.getId());
        user.store();

        Submission.removeAllAnswersForUser(user.getId(), contest);

        return redirect(routes.UserInfo.contestsList(eventId));
    }

    public static Widget getProblemsWidgets(List<List<ConfiguredProblem>> pagedUserProblems) {
        Set<ResourceLink> links = new HashSet<>();

        for (List<ConfiguredProblem> page : pagedUserProblems)
            for (ConfiguredProblem problem : page)
                links.addAll(problem.getProblem().getWidget(false).links());

        return new ListWidget(new ArrayList<>(links));
    }

    public static Widget getWidgetsForContests(User user, List<Contest> contests) {
        Set<ResourceLink> links = new HashSet<>();

        for (Contest contest : contests) {
            Widget widget = getProblemsWidgets(contest.getPagedUserProblems(user));
            links.addAll(widget.links());
        }

        return new ListWidget(new ArrayList<>(links));
    }

}
