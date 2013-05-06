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

    public static final String FIELD_CONTEST_INFO = "_contests";

    public static final PasswordGenerator passwordGenerator = new PasswordGenerator();

    private Map<String, Object> map = new HashMap<>();
    private Map<String, ContestInfoForUser> contest2info = new HashMap<>();

    public User() {
    }

    public void update(Deserializer deserializer) {
        for (String field : deserializer.fieldSet()) {
            if (field.equals(FIELD_CONTEST_INFO))
                loadContestsInfo(deserializer.getDeserializer(FIELD_CONTEST_INFO));
            else {
                Object fieldValue = deserializer.getObject(field);
                map.put(field, fieldValue);
            }
        }
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

    public boolean testPassword(String password) {
        return passwordHash(password).equals(getString(FIELD_PASS_HASH));
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

            contextArgs.put("user", user);
        }

        return user;
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

    @Override
    public void store(Serializer serializer) {
        for (Map.Entry<String, Object> field2value : map.entrySet())
            serializer.write(field2value.getKey(), field2value.getValue());

        Serializer contestInfoSerializer = serializer.getSerializer(FIELD_CONTEST_INFO);
        for (Map.Entry<String, ContestInfoForUser> id2date : contest2info.entrySet()) {
            String contestId = id2date.getKey();
            ContestInfoForUser contestInfo = id2date.getValue();
            contestInfo.store(contestInfoSerializer.getSerializer(contestId));
        }
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

    public ContestInfoForUser getContestInfoCreateIfNeeded(String contestId) {
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
        List<ConfiguredProblem> configuredUserProblems = contest.getConfiguredUserProblems(uid);

        for (ConfiguredProblem configuredUserProblem : configuredUserProblems) {
            String link = configuredUserProblem.getLink();
            Submission submission = Submission.getSubmissionForUser(contest, uid, link, Submission.AnswerOrdering.LAST, Submission.TimeType.LOCAL);

            pid2ans.add(submission == null ? null : submission.getAnswer());
        }

        return pid2ans;
    }
}