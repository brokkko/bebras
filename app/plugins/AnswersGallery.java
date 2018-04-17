package plugins;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import models.Contest;
import models.Event;
import models.Submission;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newserialization.MongoDeserializer;
import org.bson.types.ObjectId;
import play.libs.F;
import play.mvc.Result;
import views.html.solutions_view;
import views.html.submission_view;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static play.mvc.Results.*;

public class AnswersGallery extends Plugin {

    public static final String SYSTEM_SUBMISSION_KEY = "__system__";

    @Override
    public F.Promise<Result> doGet(String action, String params) {
        String[] contestAndOther = params.split("/", 2);
        if (contestAndOther.length < 1)
            return F.Promise.pure(badRequest());
        String contestId = contestAndOther[0];
        Contest contest = Event.current().getContestById(contestId);
        if (contest == null)
            return F.Promise.pure(badRequest());

        switch (action) {
            case "user":
                if (contestAndOther.length != 2)
                    return F.Promise.pure(badRequest());
                String uid = contestAndOther[1];
                return F.Promise.pure(showUser(contest, uid));
            case "contest":
                if (contestAndOther.length != 1)
                    return F.Promise.pure(badRequest());
                return F.Promise.pure(showContest(contest));
            case "view":
                if (contestAndOther.length != 2)
                    return F.Promise.pure(badRequest());
                String other = contestAndOther[1];
                return F.Promise.pure(showSubmission(contest, other));
        }

        return F.Promise.pure(badRequest());
    }

    private Result showSubmission(Contest contest, String pidAndUidAndSubmission) {
        if (!User.currentRole().hasEventAdminRight()) //TODO allow for everybody
            return forbidden();

        String[] pidAndUidAndSubmissionSplit = pidAndUidAndSubmission.split("/", 3);

        if (pidAndUidAndSubmissionSplit.length != 3)
            return badRequest();

        String uid = pidAndUidAndSubmissionSplit[0];
        String pid = pidAndUidAndSubmissionSplit[1];
        String submission = null;
        try {
            submission = URLDecoder.decode(pidAndUidAndSubmissionSplit[2], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        System.out.println("sub" + submission);

        User user;
        try {
            user = User.getUserById(new ObjectId(uid));
        } catch (IllegalArgumentException iae) {
            return badRequest("user not found");
        }
        if (user == null)
            return badRequest("user not found");

        List<ConfiguredProblem> userProblems = contest.getUserProblems(user);
        //TODO what if user has one problem several times?
        Optional<ConfiguredProblem> ocp = userProblems.stream().filter(cp -> cp.getProblemId().toString().equals(pid)).findFirst();
        if (!ocp.isPresent())
            return notFound("pid not found " + pid);
        ConfiguredProblem cp = ocp.get();

        return ok(submission_view.render(user, contest.getId(), cp, submission));
    }

    private Result showUser(Contest contest, String uid) {
        //group by problem

        User adminUser = User.current();

        if (adminUser == null)
            return unauthorized();

        User user;
        try {
             user = User.getUserById(new ObjectId(uid));
        } catch (IllegalArgumentException iae) {
            return badRequest("user not found");
        }

        if (!adminUser.hasEventAdminRight() && !adminUser.isUpper(user))
            return unauthorized("you don't have any rights");

//        List<ConfiguredProblem> userProblems = contest.getUserProblems(user);

        Map<ObjectId, List<Submission>> pid2subs = getAllSubmissions(contest, "u", user.getId()).stream()
                .sorted(Comparator.comparingLong(Submission::getLocalTime)) // not sure this is needed
                .collect(Collectors.groupingBy(Submission::getProblemId));


        return ok(solutions_view.render(this, contest, pid2subs));
    }

    private Result showContest(Contest contest) {
        User adminUser = User.current();
        if (adminUser == null)
            return unauthorized();

        if (!adminUser.hasEventAdminRight())
            return unauthorized("you don't have any rights");

        Map<ObjectId, List<Submission>> pid2subs = getAllSubmissions(contest).stream()
                .sorted(Comparator.comparingLong(Submission::getLocalTime)) // not sure this is needed
                .collect(Collectors.groupingBy(Submission::getProblemId));

        return ok(solutions_view.render(this, contest, pid2subs));
    }

    @Override
    public void initPage() {
    }

    @Override
    public void initEvent(Event event) {
    }

    private List<Submission> getAllSubmissions(Contest contest, Object... nameValue) {
        if (nameValue.length % 2 != 0)
            throw new IllegalArgumentException("There should be an even number of names and values");

        DBCollection submissionsCollection = contest.getCollection();

        List<Submission> allSubmissions = new ArrayList<>();

        DBObject query = new BasicDBObject();
        query.put("pid", new BasicDBObject("$ne", null)); //TODO allow admin view system submissions
        for (int i = 0; i < nameValue.length; i += 2) {
            String name = (String)nameValue[i];
            Object value = nameValue[i + 1];
            query.put(name, value);
        }

        DBObject sort = new BasicDBObject("pid", 1);
        sort.put("lt", 1);

        //TODO this is a duplication with User.evaluateAllSubmissions. Move to Submission
        try (
                DBCursor submissionsCursor = submissionsCollection.find(query).sort(sort)
        ) {
            long previousLocalTime = -1;
            ObjectId previousPid = null;

            while (submissionsCursor.hasNext()) {
                Submission submission = new Submission(contest, new MongoDeserializer(submissionsCursor.next()));

                //local time may be the same if contestant sent the same several times
                if (submission.getLocalTime() == previousLocalTime && submission.getProblemId() == previousPid)
                    continue;

                previousLocalTime = submission.getLocalTime();
                previousPid = submission.getProblemId();

                allSubmissions.add(submission);
            }
        }

        return allSubmissions;
    }

}
