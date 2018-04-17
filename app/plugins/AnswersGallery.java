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
import play.mvc.Results;
import views.html.solutions_view;
import views.html.submission_view;

import java.util.*;
import java.util.stream.Collectors;

import static play.mvc.Results.*;

public class AnswersGallery extends Plugin {

    public static final String SYSTEM_SUBMISSION_KEY = "__system__";

    @Override
    public F.Promise<Result> doGet(String action, String params) {
        //event = [contest]
        //contest = [problem]
        //problem = [user type]
        //user type = [user type]

        String[] contestAndOther = params.split("/", 2);
        if (contestAndOther.length != 2)
            return F.Promise.pure(badRequest());
        String contestId = contestAndOther[0];
        Contest contest = Event.current().getContestById(contestId);
        if (contest == null)
            return F.Promise.pure(badRequest());

        String other = contestAndOther[1];

        switch (action) {
            case "user":
                return F.Promise.pure(showUser(contest, other));
            case "problem":
                return F.Promise.pure(showProblem(contest, other));
            case "view":
                return F.Promise.pure(showSubmission(contest, other));
        }

        return F.Promise.pure(badRequest());
    }

    private Result showSubmission(Contest contest, String pidAndUidAndSubmission) {
        String[] pidAndUidAndSubmissionSplit = pidAndUidAndSubmission.split("/", 3);

        if (pidAndUidAndSubmissionSplit.length != 3)
            return badRequest();

        String uid = pidAndUidAndSubmissionSplit[0];
        String pid = pidAndUidAndSubmissionSplit[1];
        String submission = pidAndUidAndSubmissionSplit[2];

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

    private Result showProblem(Contest contest, String pid) {
        //group by user types, in each types sort by rank

        return null;
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
