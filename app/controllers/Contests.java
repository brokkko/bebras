package controllers;

import controllers.actions.*;
import models.Contest;
import models.Submission;
import models.User;
import models.UserType;
import models.newproblems.Problem;
import models.newserialization.JSONDeserializer;
import models.newserialization.JSONSerializer;
import models.newserialization.ListSerializer;
import models.newserialization.Serializer;
import models.results.Info;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.mvc.BodyParser;
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
@DcesController
public class Contests extends Controller {

    @SuppressWarnings("UnusedParameters")
    public static Result startContest(String eventId, String contestId) {
        return ok(views.html.start_contest_confirmation.render());
    }

    public static Result contest(String eventId, String contestId) {
        User user = User.current();

        Contest contest = Contest.current();

        int status = user.getContestStatus(contest);
        if (status == 6)
            return forbidden();

        List<List<Problem>> pagedUserProblems = contest.getPagedUserProblems(user);

        List<ResourceLink> cssLinksList = getCssLinks(pagedUserProblems);
        List<ResourceLink> jsLinksList = getJsLinks(pagedUserProblems);

        List<Info> answersForContest = user.getAnswersForContest(contest);

        //fill json info with user answers
        JSONSerializer contestInfoSerializer = new JSONSerializer();
        ListSerializer problemsInfoSerializer = contestInfoSerializer.getListSerializer("problems");

        Map<Problem, Integer> problem2index = new HashMap<>();
        int index = 0;
        for (List<Problem> page : pagedUserProblems)
            for (Problem problem : page) {
                Serializer problemInfoSerializer = problemsInfoSerializer.getSerializer();
                Info answer = answersForContest.get(index);

                if (answer == null)
                    problemInfoSerializer.writeNull("ans");
                else
                    problem.getAnswerPattern().write(problemInfoSerializer, "ans", answer);
                problemInfoSerializer.write("type", problem.getType());

                problem2index.put(problem, index);

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
        contestInfoSerializer.write("status", textStatus);

        return ok(views.html.contest.render(textStatus, pagedUserProblems, problem2index, contestInfoSerializer.getNode().toString(), cssLinksList, jsLinksList));
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

            submissions.add(submission);
        }

        User user = User.current();
        user.invalidateContestResults(contestId);

        //store all submissions
        for (Submission submission : submissions)
            submission.serialize();

        return ok();
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

        if (!contest.isAllowRestart() && User.current().getType() != UserType.EVENT_ADMIN)
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

    //TODO this is almost the same as get css links
    private static List<ResourceLink> getJsLinks(List<List<Problem>> pagedUserProblems) {
        Set<String> jsLinks = new HashSet<>();

        for (List<Problem> page : pagedUserProblems)
            for (Problem problem : page)
                jsLinks.add(problem.getType() + ".problem");

        List<ResourceLink> jsLinksList = new ArrayList<>();
        for (String jsLink : jsLinks)
            jsLinksList.add(new ResourceLink(jsLink, "js"));
        return jsLinksList;
    }

    private static List<ResourceLink> getCssLinks(List<List<Problem>> pagedUserProblems) {
        Set<String> cssLinks = new HashSet<>();

        for (List<Problem> page : pagedUserProblems)
            for (Problem problem : page)
                cssLinks.add(problem.getType() + ".problem");

        List<ResourceLink> cssLinksList = new ArrayList<>();
        for (String cssLink : cssLinks)
            cssLinksList.add(new ResourceLink(cssLink, "css"));
        return cssLinksList;
    }

}
