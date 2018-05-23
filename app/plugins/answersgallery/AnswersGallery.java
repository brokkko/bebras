package plugins.answersgallery;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import models.Contest;
import models.Event;
import models.Submission;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.ProblemInfo;
import models.newserialization.MongoDeserializer;
import org.bson.types.ObjectId;
import play.libs.F;
import play.mvc.Result;
import plugins.Plugin;
import views.html.solutions_view;
import views.html.submission_view;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                String sid = contestAndOther[1];
                return F.Promise.pure(showSubmission(contest, sid));
        }

        return F.Promise.pure(badRequest());
    }

    private Result showSubmission(Contest contest, String submissionIdString) {
        ObjectId submissionId;
        try {
            submissionId = new ObjectId(submissionIdString);
        } catch (IllegalArgumentException iae) {
            return badRequest("ill-formed submission id");
        }
        Submission submission = Submission.getSubmissionById(contest, submissionId);
        if (submission == null)
            return badRequest("unknown submission id");

        User user = User.getUserById(submission.getUser());
        if (user == null)
            return badRequest("user not found");

        List<ConfiguredProblem> userProblems = contest.getUserProblems(user);

        ObjectId pid = submission.getProblemId();
        //TODO what if user has one problem several times?
        Optional<ConfiguredProblem> ocp = userProblems.stream().filter(cp -> cp.getProblemId().equals(pid)).findFirst();
        if (!ocp.isPresent())
            return notFound("pid not found " + pid);
        ConfiguredProblem cp = ocp.get();

        String jsonAnswer = cp.getProblem().getAnswerPattern().toJSON(submission.getAnswer());
        System.out.println(submission.getAnswer().get("sol"));
        System.out.println(submission.getAnswer().get("res"));

        return ok(submission_view.render(user, contest.getId(), cp, jsonAnswer));
    }

    private Map<ObjectId, List<SubmissionAndCheck>> processSubmissionsStream(Stream<Submission> stream) {
        return stream.sorted(Comparator.comparingLong(Submission::getLocalTime)) // not sure this is needed
                .map(SubmissionAndCheck::new)
                .collect(Collectors.groupingBy(sac -> sac.getSubmission().getProblemId()));
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

        Map<ObjectId, List<SubmissionAndCheck>> pid2subs = processSubmissionsStream(
                getAllSubmissions(contest, "u", user.getId()).stream()
        );

        return ok(solutions_view.render(this, contest, pid2subs, adminUser));
    }

    private Result showContest(Contest contest) {
        User adminUser = User.current();
        if (adminUser == null)
            return unauthorized();

//        if (!adminUser.hasEventAdminRight())
//            return unauthorized("you don't have any rights");

        Stream<Submission> usersLastSubmissions = getAllSubmissions(contest).stream().collect(Collectors.groupingBy(
                Submission::getUser,
                Collectors.reducing(null, (x, y) -> y)
        )).values().stream();

        Map<ObjectId, List<SubmissionAndCheck>> pid2subs = processSubmissionsStream(usersLastSubmissions);

        pid2subs.forEach((oid, subs) -> {
            ProblemInfo problemInfo = ProblemInfo.get(oid);
            if (problemInfo == null)
                return;

            subs.sort((s1, s2) -> problemInfo.getProblem().comparator().compare(s2.getCheck(), s1.getCheck()));
        });

        return ok(solutions_view.render(this, contest, pid2subs, adminUser));
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
            String name = (String) nameValue[i];
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
