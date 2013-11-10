package models;

import com.mongodb.*;
import controllers.actions.AuthenticatedAction;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.ProblemInfo;
import models.newserialization.*;
import models.results.Info;
import models.results.InfoPattern;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.10.12
 * Time: 14:42
 */
public class Submission implements Serializable {

    public static final String USER_FIELD = "u";
    public static final String LOCAL_TIME_FIELD = "lt";
    public static final String SERVER_TIME_FIELD = "st";
    public static final String ANSWER_FIELD = "a";
    public static final String PROBLEM_ID_FIELD = "pid";
    public static final String PROBLEM_NUM_FIELD = "pn";

    private static final InfoPattern systemPattern = new InfoPattern(
            "f", new BasicSerializationType<>(String.class), "field",
            "v", new BasicSerializationType<>(String.class), "value"
    );

    public static enum TimeType {
        LOCAL,
        SERVER
    }

    public static enum AnswerOrdering {
        FIRST,
        LAST
    }

    private Contest contest;

    private ObjectId user;
    private long localTime; /* time in milliseconds from the beginning*/
    private Date serverTime;
    private ObjectId problemId;
    private Info answer;

    public static Submission getSubmissionForUser(Contest contest, ObjectId user, ObjectId problemId, AnswerOrdering answerOrdering, TimeType timeType) {
        DBCollection collection = contest.getCollection();

        BasicDBObject query = new BasicDBObject();
        query.put(USER_FIELD, user);

        if (problemId != null)
            query.put(PROBLEM_ID_FIELD, problemId);

        try (DBCursor cursor = collection.find(query).sort(
                new BasicDBObject(
                        timeType == TimeType.LOCAL ? LOCAL_TIME_FIELD : SERVER_TIME_FIELD,
                        answerOrdering == AnswerOrdering.FIRST ? 1 : -1
                ))
        ) {
            if (cursor.hasNext())
                return new Submission(user, contest, new MongoDeserializer(cursor.next()));
            else
                return null;
        }
    }

    public Submission(Contest contest, Deserializer deserializer) {
        this(readUserFromDeserializer(deserializer), contest, deserializer);
    }

    private static ObjectId readUserFromDeserializer(Deserializer deserializer) {
        ObjectId user = deserializer.readObjectId(USER_FIELD);
        if (user == null)
            user = User.current().getId();
        return user;
    }

    public Submission(Contest contest, ObjectId user, long localTime, Date serverTime, ObjectId problemId, Info answer) {
        this.contest = contest;
        this.user = user;
        this.localTime = localTime;
        this.serverTime = serverTime;
        this.problemId = problemId;
        this.answer = answer;
    }

    //user = null means current user
    public Submission(ObjectId user, Contest contest, Deserializer deserializer) {
        this.user = user;
        this.contest = contest;

        serverTime = deserializer.readDate(SERVER_TIME_FIELD);
        problemId = deserializer.readObjectId(PROBLEM_ID_FIELD);
        Integer problemNumber = deserializer.readInt(PROBLEM_NUM_FIELD);

        //read local time either long or int
        localTime = deserializer.readLong(LOCAL_TIME_FIELD);

        populateAbsentData(contest, problemNumber);

        //load answer
        Problem problem = problemId == null ? null : ProblemInfo.get(problemId).getProblem();

        if (problem == null) //means system message
            answer = systemPattern.read(deserializer, ANSWER_FIELD);
        else
            answer = problem.getAnswerPattern().read(deserializer, ANSWER_FIELD);
    }

    private void populateAbsentData(Contest contest, Integer problemNumber) {
        //here we call current user only if it is really needed

        if (user == null)
            user = User.current().getId();

        if (serverTime == null)
            serverTime = AuthenticatedAction.getRequestTime();

        if (problemId == null && problemNumber != null) {
            int pid = problemNumber;
            ConfiguredProblem problem = contest.getUserProblems(User.current()).get(pid);

            problemId = problem.getProblemId();
        }
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write(USER_FIELD, user);
        serializer.write(LOCAL_TIME_FIELD, localTime);
        serializer.write(SERVER_TIME_FIELD, serverTime);
        serializer.write(PROBLEM_ID_FIELD, problemId);

        if (problemId != null) {
            Problem problem = ProblemInfo.get(problemId).getProblem();
            problem.getAnswerPattern().write(serializer, ANSWER_FIELD, answer);
        } else
            systemPattern.write(serializer, ANSWER_FIELD, answer);
    }

    public void serialize() {
        MongoSerializer serializer = new MongoSerializer();
        serialize(serializer);

        contest.getCollection().save(serializer.getObject());
    }

    public Contest getContest() {
        return contest;
    }

    public ObjectId getUser() {
        return user;
    }

    public long getLocalTime() {
        return localTime;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public ObjectId getProblemId() {
        return problemId;
    }

    public Info getAnswer() {
        return answer;
    }

    public boolean isSystem() {
        return problemId == null;
    }

    public String getSystemField() {
        return (String) answer.get("f");
    }

    public String getSystemValue() {
        return (String) answer.get("v");
    }

    public static void removeAllAnswersForUser(ObjectId userId, Contest contest) {
        DBCollection data = contest.getCollection();

        BasicDBObject removeObject = new BasicDBObject();
        removeObject.put(USER_FIELD, userId);

        data.remove(removeObject);
    }

}
