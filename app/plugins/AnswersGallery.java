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
import models.newproblems.ProblemInfo;
import models.newserialization.MongoDeserializer;
import org.bson.types.ObjectId;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import views.html.solutions_view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.unauthorized;

public class AnswersGallery extends Plugin {

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
        }

        return F.Promise.pure(badRequest());
    }

    private Result showUser(Contest contest, String uid) {
        //group by problem

        System.out.println("here 2");

        User adminUser = User.current();

        if (adminUser == null)
            return unauthorized();

        User user;
        System.out.println("here 2");
        try {
             user = User.getUserById(new ObjectId(uid));
        } catch (IllegalArgumentException iae) {
            System.out.println("here 1");
            return badRequest("user not found");
        }

        if (!adminUser.hasEventAdminRight() && !adminUser.isUpper(user))
            return unauthorized("you don't have any rights");

        List<ConfiguredProblem> userProblems = contest.getUserProblems(user);

        Map<String, List<Submission>> pid2subs = getAllSubmissions(contest, "u", user.getId()).stream()
                .sorted(Comparator.comparingLong(Submission::getLocalTime)) // not sure this is needed
                .collect(Collectors.groupingBy(
                        submission -> {
                            String problemName = contest.getProblemName(submission.getProblemId());
                            return problemName == null ? "NULL" : problemName;
                        }
                ));


        return Results.ok(solutions_view.render(pid2subs));
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
        query.put("pid", new BasicDBObject("$ne", null)); //TODO all queries ask for non null
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
