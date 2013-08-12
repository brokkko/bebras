package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.data.TableDescription;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newproblems.newproblemblock.ProblemBlock;
import models.newserialization.*;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.results.CombinedTranslator;
import models.results.EmptyTranslator;
import models.results.InfoPattern;
import models.results.Translator;
import org.bson.types.ObjectId;
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
    private boolean onlyAdmin = false;
    private boolean allowRestart; //TODO make this allowed restarts count

    private List<Integer> pageSizes;
    private List<ProblemBlock> problemBlocks;

    private CombinedTranslator resultTranslator;
    private List<Translator> resultTranslators;

    private List<TableDescription> tables;

    private Map<String, String> pid2name = new HashMap<>();

    public Contest(Event event, Deserializer deserializer) {
        this.event = event;

        id = deserializer.readString("id");
        name = deserializer.readString("name");
        description = deserializer.readString("description");

        start = deserializer.readDate("start");
        finish = deserializer.readDate("finish");
        results = deserializer.readDate("results");

        onlyAdmin = deserializer.readBoolean("only admin", true);

        duration = deserializer.readInt("duration");

        allowRestart = deserializer.readBoolean("allow restart", false);

        pageSizes = SerializationTypesRegistry.list(int.class).read(deserializer, "page sizes");

        problemBlocks = SerializationTypesRegistry.list(SerializationTypesRegistry.PROBLEM_BLOCK).read(deserializer, "blocks");

        //read results translator

        resultTranslators = SerializationTypesRegistry.list(SerializationTypesRegistry.TRANSLATOR).read(deserializer, "results translators");
        if (resultTranslators.size() == 0)
            resultTranslators.add(new EmptyTranslator());
        resultTranslator = new CombinedTranslator(resultTranslators);

        tables = SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).read(deserializer, "tables");

        pid2name = SerializationTypesRegistry.map(String.class).read(deserializer, "pid2name");
    }

    public static Contest deserialize(Event event, Deserializer deserializer) {
        return new Contest(event, deserializer);
    }

    public void serialize(Serializer serializer) {
        serializer.write("id", id);
        serializer.write("name", name);
        serializer.write("description", description);

        serializer.write("start", start);
        serializer.write("finish", finish);
        serializer.write("results", results);

        serializer.write("only admin", onlyAdmin);
        serializer.write("duration", duration);
        serializer.write("allow restart", allowRestart);

        SerializationTypesRegistry.list(int.class).write(serializer, "page sizes", pageSizes);
        SerializationTypesRegistry.list(SerializationTypesRegistry.PROBLEM_BLOCK).write(serializer, "blocks", problemBlocks);

        SerializationTypesRegistry.list(SerializationTypesRegistry.TRANSLATOR).write(serializer, "results translators", resultTranslators);

        SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).write(serializer, "tables", tables);

        SerializationTypesRegistry.map(String.class).write(serializer, "pid2name", pid2name);
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
        if (results == null)
            return event.getResults();
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

    public List<TableDescription> getTables() {
        return tables;
    }

    public TableDescription getTable(int index) {
        return index < 0 || index >= tables.size() ? null : tables.get(index);
    }

    public List<ProblemBlock> getProblemBlocks() {
        return problemBlocks;
    }

    public DBCollection getCollection() {
        if (event.getId().equals("bbtc"))
            return MongoConnection.getCollection(CONTEST_COLLECTION_NAME_PREFIX + event.getId() + "-" + id);
        else
            return MongoConnection.getCollection(CONTEST_COLLECTION_NAME_PREFIX + event.getId() + "_" + id);
    }

    public boolean isUnlimitedTime() {
        return duration == 0;
    }

    public boolean resultsAvailableImmediately() {
        return getResults().before(start);
    }

    //TODO move to user
    public boolean resultsNotAvailableAtAll() {
        return getResults().getTime() - finish.getTime() > 1000l * 60 * 60 * 24 * 265 * 50; // 50 years
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

    public List<ConfiguredProblem> getAllPossibleProblems() {
        List<ConfiguredProblem> result = new ArrayList<>();
        for (ProblemBlock problemBlock : problemBlocks)
            result.addAll(problemBlock.getAllPossibleProblems(this));

        return result;
    }

    public int getProblemsCount() {
        int problemsCount = 0;
        for (ProblemBlock problemBlock : problemBlocks)
            problemsCount += problemBlock.getProblemsCount();
        return problemsCount;
    }

    public List<ConfiguredProblem> getUserProblems(User user) {
        List<ConfiguredProblem> problems = new ArrayList<>();

        for (ProblemBlock problemBlock : problemBlocks)
            problems.addAll(problemBlock.getProblems(this, user));

        return problems;
    }

    public List<List<Problem>> getPagedUserProblems(User user) {
        List<List<Problem>> pages = new ArrayList<>();
        List<ConfiguredProblem> userProblems = getUserProblems(user);

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
                aPage.add(userProblems.get(index).getProblem());

            pages.add(aPage);
        }

        return pages;
    }

    public long getDurationInMs() {
        return getDuration() * 60l * 1000l;
    }

    public Translator getResultTranslator() {
        return resultTranslator;
    }

    public InfoPattern getResultsInfoPattern() {
        return resultTranslator.getInfoPattern();
    }

    public boolean isOnlyAdmin() {
        return onlyAdmin;
    }

    public String getProblemName(ObjectId pid) {
        return pid == null ? null : pid2name.get(pid.toString());
    }

    public void registerProblemName(ObjectId pid, String name) {
        pid2name.put(pid.toString(), name);
    }

    // form

    public void updateFromContestChangeForm(Deserializer deserializer) {
        name = deserializer.readString("name");
        start = deserializer.readDate("start");
        finish = deserializer.readDate("finish");
        results = deserializer.readDate("results");
        duration = deserializer.readInt("duration", 0);
        description = deserializer.readString("description", null);
        onlyAdmin = deserializer.readBoolean("only admin", false);
        allowRestart = deserializer.readBoolean("allow restart", false);

        pageSizes = SerializationTypesRegistry.list(int.class).read(deserializer, "page sizes");

        tables = SerializationTypesRegistry.list(new SerializableSerializationType<>(TableDescription.class)).read(deserializer, "tables");
    }

    public RawForm saveToForm(InputForm form) {
        FormSerializer serializer = new FormSerializer(form);
        serialize(serializer);
        return serializer.getRawForm();
    }

    // statistics

    public long getNumStarted() {
        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());

        String sd = "_contests." + id + ".sd";
        query.put(sd, new BasicDBObject(new BasicDBObject("$exists", true)));
        query.put(sd, new BasicDBObject("$ne", null));

        return MongoConnection.getUsersCollection().count(query);
    }
}