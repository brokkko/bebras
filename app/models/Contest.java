package models;

import com.mongodb.DBCollection;
import controllers.MongoConnection;
import models.problems.Problem;
import models.problems.problemblock.FolderBlock;
import models.problems.problemblock.OneProblemBlock;
import models.problems.problemblock.ProblemBlock;
import models.problems.problemblock.RandomProblemsBlock;
import models.serialization.Deserializer;
import models.serialization.ListDeserializer;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private List<Integer> pageSizes;
    private List<ProblemBlock> problemBlocks;

    public Contest(Event event, Deserializer deserializer) {
        this.event = event;

        id = deserializer.getString("id");
        name = deserializer.getString("name");
        description = deserializer.getString("description");

        start = Utils.parseSimpleTime(deserializer.getString("start"));
        finish = Utils.parseSimpleTime(deserializer.getString("finish"));
        results = Utils.parseSimpleTime(deserializer.getString("results"));

        duration = deserializer.getInt("duration");

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
            ProblemBlock[] blocks = {new RandomProblemsBlock(), new FolderBlock(), new OneProblemBlock()};
            for (ProblemBlock block : blocks)
                if (block.acceptsConfiguration(configuration)) {
                    problemBlocks.add(block);
                    break;
                }
        }
    }

    public List<Problem> getUserProblems(String userId) {
        List<Problem> problems = new ArrayList<>();

        for (ProblemBlock problemBlock : problemBlocks)
            for (Problem problem : problemBlock.getProblems(userId))
                problems.add(problem);

        return problems;
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

    public DBCollection getCollection() {
        return MongoConnection.getCollection(CONTEST_COLLECTION_NAME_PREFIX + event.getId() + "-" + id);
    }

    public boolean contestStarted() {
        return start.before(new Date());
    }

    public boolean contestFinished() {
        return finish.before(new Date());
    }

    public boolean resultsAvailable() {
        return results.before(new Date());
    }

    public boolean isUnlimitedTime() {
        return duration == 0;
    }

    public boolean resultsAvailableImmediately() {
        return results.before(start);
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
}