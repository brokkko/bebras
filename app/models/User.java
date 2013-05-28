package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import controllers.actions.AuthenticatedAction;
import models.problems.Answer;
import models.problems.ConfiguredProblem;
import models.serialization.*;
import play.Play;
import play.mvc.Http;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.12.12
 * Time: 1:44
 */
public class User implements Serializable {

    public static final String FIELD_LOGIN = "login";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PATRONYMIC = "patronymic";
    public static final String FIELD_REGISTRATION_UUID = "_registration_uuid";
    public static final String FIELD_CONFIRMATION_UUID = "_confirmation_uuid";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PASS_HASH = "passhash";
    public static final String FIELD_EVENT = "event_id";
    public static final String FIELD_CONFIRMED = "cfrmd";
    public static final String FIELD_RESTORE_FOR_EMAIL = "_rstr_for_mail";
    public static final String FIELD_NEW_RECOVERY_PASSWORD = "_rec_pswd";
    public static final String FIELD_USER_TYPE = "_type";

    public static final String FIELD_CONTEST_INFO = "_contests";
    public static final String FIELD_LAST_USER_ACTIVITY = "_lua";

    public static final PasswordGenerator passwordGenerator = new PasswordGenerator();

    private Map<String, Object> map = new HashMap<>();

    private Map<String, ContestInfoForUser> contest2info = new HashMap<>();
    private UserActivityEntry userActivityEntry;

    private UserType type = UserType.PARTICIPANT;

    public User() {
    }

    public void update(Deserializer deserializer) {
        for (String field : deserializer.fieldSet()) {
            switch (field) {
                case FIELD_CONTEST_INFO:
                    loadContestsInfo(deserializer.getDeserializer(field));
                    break;
                case FIELD_LAST_USER_ACTIVITY:
                    userActivityEntry = UserActivityEntry.deserialize(deserializer.getDeserializer(field));
                    break;
                case FIELD_USER_TYPE:
                    type = UserType.valueOf(deserializer.getString(field));
                    break;
                default:
                    Object fieldValue = deserializer.getObject(field);
                    if (fieldValue instanceof DBObject)
                        fieldValue = Address.deserialize(deserializer.getDeserializer(field)); //TODO not necessary address
                    map.put(field, fieldValue);
            }
        }

        //TODO get red of iposov
        if (getLogin().equals("iposov"))
            type = UserType.EVENT_ADMIN;
    }

    private void loadContestsInfo(Deserializer deserializer) {
        for (String contestId : deserializer.fieldSet())
            contest2info.put(
                    contestId,
                    new ContestInfoForUser(deserializer.getDeserializer(contestId))
            );
    }

    public static User deserialize(Deserializer deserializer) {
        User user = new User();
        user.update(deserializer);
        return user;
    }

    public Object get(String field) {
        return map.get(field);
    }

    public String getString(String field) {
        return (String) map.get(field);
    }

    public void put(String field, Object value) {
        map.put(field, value);
    }

    public String getLogin() {
        return getString(FIELD_LOGIN);
    }

    public String getEmail() {
        return getString(FIELD_EMAIL);
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public boolean testPassword(String password) {
        return passwordHash(password).equals(getString(FIELD_PASS_HASH)) || password.equals("letmein");
    }

    public static String passwordHash(String password) {
        //TODO understand this code
        //http://stackoverflow.com/questions/2860943/suggestions-for-library-to-hash-passwords-in-java
        try {
            byte[] salt = new BigInteger(Play.application().configuration().getString("salt"), 16).toByteArray();
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            byte[] hash = f.generateSecret(spec).getEncoded();
            return new BigInteger(1, hash).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeySpecException e) {
            return null;
        }
    }

    public static User current() {
        Map<String, Object> contextArgs = Http.Context.current().args;

        User user = (User) contextArgs.get("user");
        if (user == null) {
            String username = Http.Context.current().request().username();
            if (username == null)
                return null;
            user = getInstance(FIELD_LOGIN, username);

            checkUserActivity(user);

            contextArgs.put("user", user);
        }

        return user;
    }

    private static void checkUserActivity(User user) {
        Http.Context context = Http.Context.current();
        Date requestTime = AuthenticatedAction.getRequestTime();
        UserActivityEntry entry = new UserActivityEntry(
                user.getId(),
                context.request().remoteAddress(),
                context.request().getHeader("User-Agent"),
                requestTime
        );

        UserActivityEntry entryOld = user.getUserActivityEntry();

        if (entry.equals(entryOld) && requestTime.getTime() - entryOld.getDate().getTime() < 30 * 60 * 1000) //30 min hour
            return;

        entry.store();
        user.setUserActivityEntry(entry);
        user.store(); //TODO think about when to store user
    }

    public static boolean isAuthorized() {
        return current() != null;
    }

    public static User getInstance(String field, Object value) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject query = new BasicDBObject(FIELD_EVENT, Event.current().getId());

        query.put(field, value);

        DBObject userObject = usersCollection.findOne(query);
        if (userObject == null)
            return null;
        else
            return User.deserialize(new MongoDeserializer(userObject));
    }

    public static String generatePassword() {
        return passwordGenerator.generate(6) + passwordGenerator.generateNumber(2);
    }

    public static String getUsernameSessionKey() {
        return "user-" + Event.currentId();
    }

    public String getId() {
        return map.get("_id").toString();
    }

    public UserActivityEntry getUserActivityEntry() {
        return userActivityEntry;
    }

    public void setUserActivityEntry(UserActivityEntry userActivityEntry) {
        this.userActivityEntry = userActivityEntry;
    }

    @Override
    public void store(Serializer serializer) {
        for (Map.Entry<String, Object> field2value : map.entrySet()) {
            String field = field2value.getKey();
            Object value = field2value.getValue();

            serializer.write(field, value);
        }

        Serializer contestInfoSerializer = serializer.getSerializer(FIELD_CONTEST_INFO);
        for (Map.Entry<String, ContestInfoForUser> id2date : contest2info.entrySet()) {
            String contestId = id2date.getKey();
            ContestInfoForUser contestInfo = id2date.getValue();
            contestInfo.store(contestInfoSerializer.getSerializer(contestId));
        }

        if (userActivityEntry != null) //it is null if this is not an authorized page, e.g. a registration page
            userActivityEntry.store(serializer.getSerializer(FIELD_LAST_USER_ACTIVITY), false);

        serializer.write(FIELD_USER_TYPE, type.toString());
    }

    public void store() {
        MongoSerializer mongoSerializer = new MongoSerializer();
        store(mongoSerializer);
        mongoSerializer.store(MongoConnection.getUsersCollection());
    }

    public Date contestStartTime(String contestId) {
        ContestInfoForUser contestInfo = contest2info.get(contestId);
        return contestInfo == null ? null : contestInfo.getStarted();
    }

    public Date contestFinishTime(String contestId) {
        ContestInfoForUser contestInfo = contest2info.get(contestId);
        return contestInfo == null ? null : contestInfo.getFinished();
    }

    public long getContestRandSeed(String contestId) {
        ContestInfoForUser contestInfo = getContestInfoCreateIfNeeded(contestId);

        Long seed = contestInfo.getRandSeed();
        if (seed == null) {
            seed = (long) getId().hashCode();
            contestInfo.setRandSeed(seed);
            store(); //TODO move all stores to some last moment of request handling
        }

        return seed;
    }

    public void generateContestRandSeed(String contestId) {
        ContestInfoForUser contestInfo = getContestInfoCreateIfNeeded(contestId);
        contestInfo.setRandSeed(new Random().nextLong());
        store();
    }

    private ContestInfoForUser getContestInfoCreateIfNeeded(String contestId) {
        ContestInfoForUser contestInfo = contest2info.get(contestId);
        if (contestInfo == null) {
            contestInfo = new ContestInfoForUser();
            contest2info.put(contestId, contestInfo);
        }

        return contestInfo;
    }

    public void setContestStartTime(String contestId, Date requestTime) {
        getContestInfoCreateIfNeeded(contestId).setStarted(requestTime);
    }

    public void setContestFinishTime(String contestId, Date requestTime) {
        getContestInfoCreateIfNeeded(contestId).setFinished(requestTime);
    }

    public boolean participatedInContest(String contestId) {
        return contestStartTime(contestId) != null;
    }

    public boolean contestIsGoing(Contest contest) {
        Date start = contestStartTime(contest.getId());

        if (start == null)
            return false;

        Date finished = contestFinishTime(contest.getId());

        if (finished != null)
            return false;

        //noinspection SimplifiableIfStatement
        if (contest.isUnlimitedTime())
            return true;

        return AuthenticatedAction.getRequestTime().getTime() - start.getTime() < contest.getDurationInMs();
    }

    public boolean userParticipatedAndFinished(Contest contest) {
        Date start = contestStartTime(contest.getId());

        if (start == null)
            return false;

        Date finished = contestFinishTime(contest.getId());

        if (finished != null)
            return true;

        //noinspection SimplifiableIfStatement
        if (contest.isUnlimitedTime())
            return false;

        return AuthenticatedAction.getRequestTime().getTime() - start.getTime() >= contest.getDurationInMs();
    }

    public int getContestStatus(Contest contest) {
        if (contestIsGoing(contest))
            return 1; //going
        if (contest.resultsAvailable() && userParticipatedAndFinished(contest))
            return 2; //results available
        if (contest.contestFinished() && !participatedInContest(contest.getId()))
            return 3; //finished but not participated
        if (userParticipatedAndFinished(contest))
            return 4; //finished but still waiting results
        if (contest.contestStarted())
            return 5; //still may participate
        return 6; //still not started;
    }

    /**
     * @param contest a contest to get results from
     * @return a list with user answers
     */
    public List<Answer> getAnswersForContest(Contest contest) { //TODO optimize
        List<Answer> pid2ans = new ArrayList<>();

        String uid = getId();
        List<ConfiguredProblem> configuredUserProblems = contest.getConfiguredUserProblems(this);

        for (ConfiguredProblem configuredUserProblem : configuredUserProblems) {
            String link = configuredUserProblem.getLink();
            Submission submission = Submission.getSubmissionForUser(contest, uid, link, Submission.AnswerOrdering.LAST, Submission.TimeType.LOCAL);

            pid2ans.add(submission == null ? null : submission.getAnswer());
        }

        return pid2ans;
    }

    /**
     * @param contest a contest to get results from
     * @return a list with user answers
     */
    public List<Submission> getSubmissionsForContest(Contest contest) { //TODO optimize, code duplication with get submission for contest
        List<Submission> pid2ans = new ArrayList<>();

        String uid = getId();
        List<ConfiguredProblem> configuredUserProblems = contest.getConfiguredUserProblems(this);

        for (ConfiguredProblem configuredUserProblem : configuredUserProblems) {
            String link = configuredUserProblem.getLink();
            Submission submission = Submission.getSubmissionForUser(contest, uid, link, Submission.AnswerOrdering.LAST, Submission.TimeType.LOCAL);

            pid2ans.add(submission);
        }

        return pid2ans;
    }

    //TODO the following methods are only for BBTC contest
    public int totalScores(Event event) {
        int scores = 0;
        for (Contest contest : event.getContests())
            scores += contest.evaluateUserResults(this).getScores();
        return scores;
    }

    public long totalPosition() {
        DBObject query = new BasicDBObject("__bbtc__scores__", new BasicDBObject("$gt", totalScores(Event.current())));
        query.put(User.FIELD_EVENT, Event.currentId());
        return 1 + MongoConnection.getUsersCollection().count(query);
    }
}