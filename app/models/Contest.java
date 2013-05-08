package models;

import com.mongodb.DBCollection;
import controllers.MongoConnection;
import controllers.actions.AuthenticatedAction;
import models.problems.Answer;
import models.problems.ConfiguredProblem;
import models.problems.Problem;
import models.problems.problemblock.FolderBlock;
import models.problems.problemblock.OneProblemBlock;
import models.problems.problemblock.ProblemBlock;
import models.problems.problemblock.RandomProblemsBlock;
import models.serialization.Deserializer;
import models.serialization.JSONSerializer;
import models.serialization.ListDeserializer;
import play.mvc.Http;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.01.13
 * Time: 13:39
 */
public class Contest {

    public static final String CONTEST_COLLECTION_NAME_PREFIX = "contest-";

    private Event event;

    private String id;
    private String name;
    private String description;

    //TODO make time more complex
    private Date start;
    private Date finish;
    private Date results;
    private int duration;
    private boolean allowRestart; //TODO make this allowed restarts count

    private List<Integer> pageSizes;
    private List<ProblemBlock> problemBlocks;

    private Map<String, Object> grader = new HashMap<>(); //TODO generalize grader

    public Contest(Event event, Deserializer deserializer) {
        this.event = event;

        id = deserializer.getString("id");
        name = deserializer.getString("name");
        description = deserializer.getString("description");

        start = Utils.parseSimpleTime(deserializer.getString("start"));
        finish = Utils.parseSimpleTime(deserializer.getString("finish"));
        results = Utils.parseSimpleTime(deserializer.getString("results"));

        duration = deserializer.getInt("duration");

        Boolean allowRestartValue = deserializer.getBoolean("allow restart");
        allowRestart = allowRestartValue == null ? false : allowRestartValue;

        //read page sizes

        ListDeserializer pageSizesDeserializer = deserializer.getListDeserializer("page sizes");
        pageSizes = new ArrayList<>();
        while (pageSizesDeserializer.hasMore())
            pageSizes.add(pageSizesDeserializer.getInt());

        //read blocks

        ListDeserializer problemsDeserializer = deserializer.getListDeserializer("problems");
        problemBlocks = new ArrayList<>();
        while (problemsDeserializer.hasMore()) {
            String configuration = problemsDeserializer.getString();
            ProblemBlock[] blocks = {new RandomProblemsBlock(this), new FolderBlock(this), new OneProblemBlock(this)};
            for (ProblemBlock block : blocks)
                if (block.acceptsConfiguration(configuration)) {
                    problemBlocks.add(block);
                    break;
                }
        }

        //load grader
        Deserializer graderDeserializer = deserializer.getDeserializer("grader");
        for (String field : graderDeserializer.fieldSet())
            grader.put(field, graderDeserializer.getObject(field));
    }

    public static Contest deserialize(Event event, Deserializer deserializer) {
        return new Contest(event, deserializer);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getStart() {
        return start;
    }

    public Date getFinish() {
        return finish;
    }

    public Date getResults() {
        return results;
    }

    public int getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    public boolean isAllowRestart() {
        return allowRestart;
    }

    public DBCollection getCollection() {
        return MongoConnection.getCollection(CONTEST_COLLECTION_NAME_PREFIX + event.getId() + "-" + id);
    }

    public boolean contestStarted() {
        return start.before(AuthenticatedAction.getRequestTime());
    }

    public boolean contestFinished() {
        return finish.before(AuthenticatedAction.getRequestTime());
    }

    public boolean resultsAvailable() {
        return results.before(AuthenticatedAction.getRequestTime());
    }

    public boolean isUnlimitedTime() {
        return duration == 0;
    }

    public boolean resultsAvailableImmediately() {
        return results.before(start);
    }

    public boolean resultsNotAvailableAtAll() {
        return results.getTime() - finish.getTime() > 1000l * 60 * 60 * 24 * 265 * 50; // 50 years
    }

    public static Contest current() {
        return current(Http.Context.current());
    }

    private static Contest current(Http.Context ctx) {
        Contest contest = (Contest) ctx.args.get("contest");

        if (contest == null) {
            //need to parse path because https://groups.google.com/forum/?fromgroups=#!topic/play-framework/sNFeqmd-mBQ
            String path = ctx.request().path();
            int firstSlash = path.indexOf('/');
            int secondSlash = path.indexOf('/', firstSlash + 1);
            int thirdSlash = path.indexOf('/', secondSlash + 1);
            if (secondSlash >= 0 && thirdSlash >= 0)
                contest = Event.current().getContestById(path.substring(secondSlash + 1, thirdSlash));

            if (contest == null)
                return null;

            ctx.args.put("contest", contest);
        }

        return contest;
    }

    public List<Problem> getUserProblems(User user) {
        List<Problem> problems = new ArrayList<>();

        for (ProblemBlock problemBlock : problemBlocks)
            for (ConfiguredProblem problem : problemBlock.getProblems(user))
                problems.add(problem.getProblem());

        return problems;
    }

    public List<ConfiguredProblem> getConfiguredUserProblems(User user) {
        List<ConfiguredProblem> problems = new ArrayList<>();

        for (ProblemBlock problemBlock : problemBlocks)
            problems.addAll(problemBlock.getProblems(user));

        return problems;
    }

    public List<List<Problem>> getPagedUserProblems(User user) {
        List<List<Problem>> pages = new ArrayList<>();
        List<Problem> userProblems = getUserProblems(user);

        int pageIndex = 0;
        int index = 0;
        while (true) {
            int block = pageSizes.get(pageIndex++);
            if (pageIndex >= pageSizes.size())
                pageIndex = 0;

            int indexesLeft = userProblems.size() - index;

            if (indexesLeft == 0)
                break;

            block = Math.min(block, indexesLeft);
            int nextIndex = index + block;
            List<Problem> aPage = new ArrayList<>(block);
            for (; index < nextIndex; index++)
                aPage.add(userProblems.get(index));

            pages.add(aPage);
        }

        return pages;
    }

    public long getDurationInMs() {
        return getDuration() * 60l * 1000l;
    }

    public ContestResult evaluateUserResults(User user) {
        List<Answer> answers = user.getAnswersForContest(this);
        List<Problem> problems = getUserProblems(user);

        int r = 0;
        int w = 0;
        int n = 0;
        int scores = 0;

        int bonus = (Integer) grader.get("right");
        int discount = (Integer) grader.get("wrong");

        for (int i = 0; i < problems.size(); i++) {
            Answer answer = answers.get(i);
            Problem problem = problems.get(i);

            if (answer == null) {
                n++;
                continue;
            }

            JSONSerializer jsonSerializer = new JSONSerializer();
            problem.check(answer, jsonSerializer);
            int res = jsonSerializer.getNode().get("result").getIntValue(); //TODO generalize this all

            if (res == 0)
                n++;
            else if (res < 0) {
                w++;
                scores += discount;
            } else {
                r++;
                scores += bonus;
            }
        }

        return new ContestResult(r, w, n, scores, bonus, discount);
    }
}