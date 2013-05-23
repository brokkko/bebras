package models;

import com.mongodb.*;
import controllers.actions.AuthenticatedAction;
import models.problems.Answer;
import models.problems.ConfiguredProblem;
import models.serialization.*;
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

    public static enum TimeType {
        LOCAL,
        SERVER
    }

    public static enum AnswerOrdering {
        FIRST,
        LAST
    }

    private Contest contest;

    private String userId;
    private long localTime; /* time in milliseconds from the beginning*/
    private Date serverTime;
    private String problemId;
    private Answer answer;

    public static Submission getSubmissionForUser(Contest contest, String userId, String problemId, AnswerOrdering answerOrdering, TimeType timeType) {
        DBCollection collection = contest.getCollection();

        BasicDBObject query = new BasicDBObject();
        query.put(USER_FIELD, userId);

        if (problemId != null)
            query.put(PROBLEM_ID_FIELD, problemId);

        try (DBCursor cursor = collection.find(query).sort(
                new BasicDBObject(
                        timeType == TimeType.LOCAL ? LOCAL_TIME_FIELD : SERVER_TIME_FIELD,
                        answerOrdering == AnswerOrdering.FIRST ? 1 : -1
                ))
        ) {
            if (cursor.hasNext())
                return new Submission(contest, new MongoDeserializer(cursor.next()));
            else
                return null;
        }
    }

    public Submission(Contest contest, String userId, long localTime, Date serverTime, String problemId, Answer answer) {
        this.contest = contest;
        this.userId = userId;
        this.localTime = localTime;
        this.serverTime = serverTime;
        this.problemId = problemId;
        this.answer = answer;
    }

    public Submission(Contest contest, Deserializer deserializer) {
        this.contest = contest;

        userId = deserializer.getString(USER_FIELD);
        serverTime = (Date) deserializer.getObject(SERVER_TIME_FIELD);
        problemId = deserializer.getObject(PROBLEM_ID_FIELD).toString();

        //read local time either long or int
        Object localTimeAsObject = deserializer.getObject(LOCAL_TIME_FIELD);
        if (localTimeAsObject instanceof Integer)
            localTime = (Integer) localTimeAsObject;
        else if (localTimeAsObject instanceof Long)
            localTime = (Long) localTimeAsObject;

        answer = new Answer();
        Deserializer answerDeserializer = deserializer.getDeserializer(ANSWER_FIELD);
        for (String field : answerDeserializer.fieldSet())
            answer.put(field, answerDeserializer.getObject(field));

        populateAbsentData();
    }

    private void populateAbsentData() {
        //here we call current user only if it is really needed

        if (userId == null)
            userId = User.current().getId();
        if (serverTime == null)
            serverTime = AuthenticatedAction.getRequestTime();

        if (problemId == null)
            throw new IllegalArgumentException("submission without problem id");

        if (!problemId.startsWith("/")) {
            int pid = Integer.parseInt(problemId);
            Contest contest = Contest.current();
            ConfiguredProblem problem = contest.getConfiguredUserProblems(User.current()).get(pid);

            problemId = problem.getLink();
        }

    }

    @Override
    public void store(Serializer serializer) {
        serializer.write(USER_FIELD, userId);
        serializer.write(LOCAL_TIME_FIELD, localTime);
        serializer.write(SERVER_TIME_FIELD, serverTime);
        serializer.write(PROBLEM_ID_FIELD, problemId);

        Serializer answerSerializer = serializer.getSerializer(ANSWER_FIELD);
        for (Map.Entry<String, Object> field2value : answer.entrySet())
            answerSerializer.write(field2value.getKey(), field2value.getValue());
    }

    public void store() {
        MongoSerializer serializer = new MongoSerializer();
        store(serializer);

        contest.getCollection().save(serializer.getObject());
    }

    public Contest getContest() {
        return contest;
    }

    public String getUserId() {
        return userId;
    }

    public long getLocalTime() {
        return localTime;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public String getProblemId() {
        return problemId;
    }

    public Answer getAnswer() {
        return answer;
    }

    public static void removeAllAnswersForUser(String userId, Contest contest) {
        DBCollection data = contest.getCollection();

        BasicDBObject removeObject = new BasicDBObject();
        removeObject.put(USER_FIELD, userId);

        data.remove(removeObject);
    }

}
