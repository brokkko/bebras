package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import controllers.actions.AuthenticatedAction;
import models.data.TableDescription;
import models.forms.InputField;
import models.forms.InputForm;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newserialization.*;
import models.results.Info;
import models.results.InfoPattern;
import models.results.Translator;
import org.bson.types.ObjectId;
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
public class User implements SerializableUpdatable {

    private static final String FIELD_PASS_HASH = "passhash";
    public static final String FIELD_EVENT = "event_id";
    public static final String FIELD_CONTEST_INFO = "_contests";
    public static final String FIELD_LAST_USER_ACTIVITY = "_lua";
    public static final String FIELD_EVENT_RESULTS = "_er";
    public static final String FIELD_USER_ROLE = "_role";
    public static final String FIELD_REGISTERED_BY = "_reg_by";

    public static final String FIELD_LOGIN = "login";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PATRONYMIC = "patronymic";
    public static final String FIELD_EMAIL = "email";

    private static final PasswordGenerator passwordGenerator = new PasswordGenerator();

    private Info info = null;
    private Event event = null; // should be substituted at update
    private String passwordHash;
    private ObjectId id;

    //TODO move this all to some extra classes
    public static final String FIELD_REGISTRATION_UUID = "_registration_uuid";
    public static final String FIELD_CONFIRMATION_UUID = "_confirmation_uuid";
    private static final String FIELD_CONFIRMED = "cfrmd";
    private static final String FIELD_RESTORE_FOR_EMAIL = "_rstr_for_mail";
    private static final String FIELD_NEW_RECOVERY_PASSWORD = "_rec_pswd";

    private String registrationUUID;
    private String confirmationUUID;
    private boolean confirmed;
    private boolean restoreForEmail;
    private String newRecoveryPassword;

    private Map<String, ContestInfoForUser> contest2info = new HashMap<>();
    private Info eventResults = null;
    private UserActivityEntry userActivityEntry;

    private UserRole role = UserRole.EMPTY;
    private ObjectId registeredBy = null;

    // cache
    private Map<Contest, List<Submission>> cachedAllSubmissions = new HashMap<>();

    public User() {
    }

    public void update(Deserializer deserializer) {
        Deserializer userActivityDeserializer = deserializer.getDeserializer(FIELD_LAST_USER_ACTIVITY);
        if (userActivityDeserializer != null)
            userActivityEntry = UserActivityEntry.deserialize(getId(), userActivityDeserializer);

        id = deserializer.readObjectId("_id");

        //read event
        String eventId = deserializer.readString(FIELD_EVENT);
        if (eventId != null) {
            event = Event.getInstance(eventId);
            if (event == null)
                throw new IllegalStateException("Deserializing user with nonexistent event " + eventId);
        } else
            event = Event.current();

        //read role
        String roleName = deserializer.readString(FIELD_USER_ROLE);
        if (roleName != null)
            role = event.getRole(roleName);

        // -------
        passwordHash = deserializer.readString(FIELD_PASS_HASH);

        info = getUserInfoPattern().read(deserializer);

        //read registration data
        registrationUUID = deserializer.readString(FIELD_REGISTRATION_UUID);
        confirmationUUID = deserializer.readString(FIELD_CONFIRMATION_UUID);
        confirmed = deserializer.readBoolean(FIELD_CONFIRMED, false);
        restoreForEmail = deserializer.readBoolean(FIELD_RESTORE_FOR_EMAIL, false);
        newRecoveryPassword = deserializer.readString(FIELD_NEW_RECOVERY_PASSWORD);

        loadContestsInfo(deserializer.getDeserializer(FIELD_CONTEST_INFO));

        eventResults = event.getResultsInfoPattern().read(deserializer, FIELD_EVENT_RESULTS);

        registeredBy = deserializer.readObjectId(FIELD_REGISTERED_BY);

        //TODO get rid of iposov
        if (getLogin().equals("iposov"))
            role = event.getRole("EVENT_ADMIN");
    }

    public void updateFromForm(FormDeserializer deserializer, InputForm form) {
        for (InputField inputField : form.getFields()) {
            String field = inputField.getName();
            info.put(field, deserializer.getValue(field));
        }
    }

    private void loadContestsInfo(Deserializer deserializer) {
        if (deserializer == null)
            return;

        for (String contestId : deserializer.fields()) {
            Contest contest = event.getContestById(contestId);

            //this may happen if some contest was removed from an event
            if (contest == null)
                continue;

            contest2info.put(
                    contestId,
                    new ContestInfoForUser(
                            contest,
                            deserializer.getDeserializer(contestId)
                    )
            );
        }
    }

    public static User deserialize(Deserializer deserializer) {
        User user = new User();
        user.update(deserializer);
        return user;
    }

    public void put(String field, Object value) {
        info.put(field, value);
    }

    public String getLogin() {
        return (String) info.get(FIELD_LOGIN);
    }

    public String getEmail() {
        return (String) info.get(FIELD_EMAIL);
    }

    public void setEmail(String email) {
        this.info.put(FIELD_EMAIL, email);
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean testPassword(String password) {
        return passwordHash(password).equals(passwordHash);
    }

    public static String passwordHash(String password) {
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

    public static UserRole currentRole() {
        User user = User.current();
        return user == null ? Event.current().getAnonymousRole() : user.getRole();
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
        user.store();
    }

    public static boolean isAuthorized() {
        return current() != null;
    }

    public static User getInstance(String field, Object value) {
        return getInstance(field, value, Event.current().getId());
    }

    public static User getInstance(String field, Object value, String eventId) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject query = new BasicDBObject(FIELD_EVENT, eventId);

        query.put(field, value);

        DBObject userObject = usersCollection.findOne(query);
        if (userObject == null)
            return null;
        else
            return User.deserialize(new MongoDeserializer(userObject));
    }

    public static User getUserById(ObjectId id) {
        return getInstance("_id", id);
    }

    public static User getUserById(String eventId, ObjectId id) {
        return getInstance("_id", id, eventId);
    }

    public static User getUserByLogin(String login) {
        return getInstance(FIELD_LOGIN, login);
    }

    public static User getUserByEmail(String email) {
        return getInstance(FIELD_EMAIL, email);
    }

    public static User getUserByRegistrationUUID(String registrationUUID) {
        return getInstance(FIELD_REGISTRATION_UUID, registrationUUID);
    }

    public static User getUserByConfirmationUUID(String confirmationUUID) {
        return getInstance(FIELD_CONFIRMATION_UUID, confirmationUUID);
    }

    public static String generatePassword() {
        return passwordGenerator.generate(6) + passwordGenerator.generateNumber(2);
    }

    public static String getUsernameSessionKey() {
        return "user-" + Event.currentId();
    }

    public ObjectId getId() {
        return id;
    }

    public UserActivityEntry getUserActivityEntry() {
        return userActivityEntry;
    }

    public void setUserActivityEntry(UserActivityEntry userActivityEntry) {
        this.userActivityEntry = userActivityEntry;
    }

    public Info getInfo() {
        return info;
    }

    @Override
    public void serialize(Serializer serializer) {
        getUserInfoPattern().write(info, serializer);

        Serializer contestInfoSerializer = serializer.getSerializer(FIELD_CONTEST_INFO);
        for (Map.Entry<String, ContestInfoForUser> id2date : contest2info.entrySet()) {
            String contestId = id2date.getKey();
            ContestInfoForUser contestInfo = id2date.getValue();
            contestInfoSerializer.write(contestId, contestInfo);
        }

        event.getResultsInfoPattern().write(serializer, FIELD_EVENT_RESULTS, eventResults);

        if (userActivityEntry != null) //it is null if this is not an authorized page, e.g. a registration page
            userActivityEntry.store(serializer.getSerializer(FIELD_LAST_USER_ACTIVITY), false);

        serializer.write(FIELD_USER_ROLE, role.getName());

        //write registration data
        serializer.write(FIELD_REGISTRATION_UUID, registrationUUID);
        serializer.write(FIELD_CONFIRMATION_UUID, confirmationUUID);
        serializer.write(FIELD_CONFIRMED, confirmed);
        serializer.write(FIELD_RESTORE_FOR_EMAIL, restoreForEmail);
        serializer.write(FIELD_NEW_RECOVERY_PASSWORD, newRecoveryPassword);

        serializer.write("_id", id);
        serializer.write(FIELD_EVENT, event.getId());
        serializer.write(FIELD_PASS_HASH, passwordHash);

        serializer.write(FIELD_REGISTERED_BY, registeredBy);
    }

    private InfoPattern getUserInfoPattern() {
        //role.getUserInfoPattern() and event plugins extra fields
        return InfoPattern.union(
                role.getUserInfoPattern(),
                event.getExtraUserFields(role.getName())
        );
    }

    public void serialize() {
        MongoSerializer mongoSerializer = new MongoSerializer();
        serialize(mongoSerializer);
        MongoConnection.getUsersCollection().save(mongoSerializer.getObject());
    }

    public void store() {
        if (MongoConnection.mayEnqueueEvents())
            MongoConnection.enqueueUserStorage(this);
        else
            serialize();
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
            contestInfo = new ContestInfoForUser(event.getContestById(contestId));
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

    //contest timing

    public boolean contestStarted(Contest contest) {
        return hasEventAdminRight() || contest.getStart().before(AuthenticatedAction.getRequestTime());
    }

    public boolean contestFinished(Contest contest) {
        return !hasEventAdminRight() && contest.getFinish().before(AuthenticatedAction.getRequestTime());
    }

    public boolean resultsAvailable(Contest contest) {
        return hasEventAdminRight() || contest.getResults().before(AuthenticatedAction.getRequestTime());
    }

    public boolean restrictedResults() {
        Date restrictedResults = event.getRestrictedResults();
        return restrictedResults != null && !hasEventAdminRight() && restrictedResults.before(AuthenticatedAction.getRequestTime());
    }

    public int getContestStatus(Contest contest) {
        if (contestIsGoing(contest))
            return 1; //going
        if (resultsAvailable(contest) && userParticipatedAndFinished(contest)) {
            if (restrictedResults())
                return 7; //results available, but they are restricted
            else
                return 2; //results available
        }
        if (contestFinished(contest) && !participatedInContest(contest.getId())) {
            if (restrictedResults())
                return 7; //results available, but they are restricted
            else
                return 3; //finished but not participated
        }
        if (userParticipatedAndFinished(contest))
            return 4; //finished but still waiting results
        if (contestStarted(contest))
            return 5; //still may participate
        return 6; //still not started;
    }

    /**
     * @param contest a contest to get results from
     * @return a list with user answers
     */
    public List<Info> getAnswersForContest(Contest contest) {
        List<Submission> submissionsForContest = getSubmissionsForContest(contest);
        List<Info> answers = new ArrayList<>(submissionsForContest.size());

        for (Submission submission : submissionsForContest)
            answers.add(submission == null ? null : submission.getAnswer());

        return answers;
    }

    /**
     * @param contest a contest to get results from
     * @return a list with user answers
     */
    public List<Submission> getSubmissionsForContest(Contest contest) {
        List<Submission> allSubmissions = getAllSubmissions(contest);

        Map<ObjectId, Submission> problem2lastSubmission = new HashMap<>();

        //iterate all submissions backwards because we need last submissions for all problems //TODO implement also best submissions instead of last
        ListIterator<Submission> li = allSubmissions.listIterator(allSubmissions.size());

        while (li.hasPrevious()) {
            Submission s = li.previous();

            ObjectId pid = s.getProblemId();
            if (!problem2lastSubmission.containsKey(pid))
                problem2lastSubmission.put(pid, s);
        }

        //put submissions into a list

        List<ConfiguredProblem> configuredUserProblems = contest.getUserProblems(this);
        List<Submission> pid2ans = new ArrayList<>();
        for (ConfiguredProblem problem : configuredUserProblems)
            pid2ans.add(problem2lastSubmission.get(problem.getProblemId()));

        return pid2ans;
    }

    public List<Submission> getAllSubmissions(Contest contest) {
        List<Submission> allSubmissions = cachedAllSubmissions.get(contest);

        if (allSubmissions == null) {
            allSubmissions = evaluateAllSubmissions(contest);
            cachedAllSubmissions.put(contest, allSubmissions);
        }

        return allSubmissions;
    }

    private List<Submission> evaluateAllSubmissions(Contest contest) {
        DBCollection submissionsCollection = contest.getCollection();

        DBObject query = new BasicDBObject("u", id);
        DBObject sort = new BasicDBObject("pid", 1);
        sort.put("lt", 1);

        List<Submission> allSubmissions = new ArrayList<>();

        try (
                DBCursor submissionsCursor = submissionsCollection.find(query).sort(sort)
        ) {
            long previousLocalTime = -1;
            while (submissionsCursor.hasNext()) {
                Submission submission = new Submission(contest, new MongoDeserializer(submissionsCursor.next()));

                //local time may be the same if contestant sent the same several times
                if (submission.getLocalTime() == previousLocalTime)
                    continue;

                allSubmissions.add(submission);
            }
        }

        return allSubmissions;
    }

    //TODO the following methods are only for BBTC contest
    public int totalScores(Event event) {
        int scores = 0;
        for (Contest contest : event.getContests()) {
            Info userResults = evaluateContestResults(contest);
            scores += (Integer) userResults.get("scores");
        }
        return scores;
    }

    public String totalPosition() {
        Object position = info.get("__bbtc__scores__");
        return position == null ? "-" : position.toString();
    }

    public String getGreeting() {
        return info.get(FIELD_NAME) + " " + info.get(FIELD_PATRONYMIC);
    }

    //registration info getters and setters

    public String getRegistrationUUID() {
        return registrationUUID;
    }

    public void setRegistrationUUID(String registrationUUID) {
        this.registrationUUID = registrationUUID;
    }

    public String getConfirmationUUID() {
        return confirmationUUID;
    }

    public void setConfirmationUUID(String confirmationUUID) {
        this.confirmationUUID = confirmationUUID;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isRestoreForEmail() {
        return restoreForEmail;
    }

    public void setRestoreForEmail(boolean restoreForEmail) {
        this.restoreForEmail = restoreForEmail;
    }

    public String getNewRecoveryPassword() {
        return newRecoveryPassword;
    }

    public void setNewRecoveryPassword(String newRecoveryPassword) {
        this.newRecoveryPassword = newRecoveryPassword;
    }

    public ObjectId getRegisteredBy() {
        return registeredBy;
    }

    public User getRegisteredByUser() {
        return registeredBy == null ? null : getUserById(registeredBy);
    }

    public void setRegisteredBy(ObjectId registeredBy) {
        this.registeredBy = registeredBy;
    }

    // tables

    public List<TableDescription<?>> getTables() {
        List<TableDescription<?>> result = new ArrayList<>();

        List<? extends TableDescription> tables = getEvent().getTables();
        for (TableDescription<?> table : tables)
            if (hasRight(table.getRight()))
                result.add(table);

        return result;
    }

    // results

    private Info evaluateContestResults(Contest contest) {
        List<Submission> submissions = getSubmissionsForContest(contest);

        List<ConfiguredProblem> problems = contest.getUserProblems(this);
        int size = problems.size();
        List<Info> problemsInfo = new ArrayList<>(size);
        List<Info> problemsSettingsInfo = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Problem problem = problems.get(i).getProblem();

            Submission submission = submissions.get(i);
            if (submission != null)
                problemsInfo.add(problem.check(submission.getAnswer())); //TODO implement remote check, online check
            else
                problemsInfo.add(null);

            problemsSettingsInfo.add(null);
        }

        return contest.getResultTranslator().translate(problemsInfo, problemsSettingsInfo, this);
    }

    public Info getContestResults(Contest contest) {
        ContestInfoForUser contestInfo = getContestInfoCreateIfNeeded(contest.getId());

        Info finalResults = contestInfo.getFinalResults();
        if (finalResults != null)
            return finalResults;

        finalResults = evaluateContestResults(contest);

        contestInfo.setFinalResults(finalResults);
        store();

        return finalResults;
    }

    public Info getEventResults() {
        if (eventResults != null)
            return eventResults;

        Translator resultTranslator = event.getResultTranslator();

        Collection<Contest> contests = event.getContests();

        List<Info> contestsInfo = new ArrayList<>(contests.size());
        List<Info> contestsSettings = new ArrayList<>(contests.size());

        for (Contest contest : contests) {
            if (contest.isAllowRestart()) //TODO invent another method how to exclude test contests
                continue;

            contestsInfo.add(getContestResults(contest));
            contestsSettings.add(null);
        }

        eventResults = resultTranslator.translate(contestsInfo, contestsSettings, this);

        store();

        return eventResults;
    }

    //rights

    public boolean hasRight(String right) {
        return role.hasRight(right);
    }

    public boolean hasEventAdminRight() {
        return hasRight("event admin");
    }

    public void invalidateEventResults() {
        eventResults = null;
        cachedAllSubmissions = null;
        store();
    }

    public void invalidateContestResults(String contestId) {
        ContestInfoForUser contestInfo = getContestInfoCreateIfNeeded(contestId);
        contestInfo.setFinalResults(null);
        invalidateEventResults();
        store();
    }

    public void invalidateAllResults() {
        for (Contest contest : event.getContests())
            getContestInfoCreateIfNeeded(contest.getId()).setFinalResults(null);
        invalidateEventResults();
    }

    private static void invalidateOneContestResults(Event event, Contest contest) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();
        DBObject query = new BasicDBObject("event_id", event.getId());
        String contestResultsField = User.FIELD_CONTEST_INFO + "." + contest.getId() + ".res";
        DBObject update = new BasicDBObject("$unset", new BasicDBObject(contestResultsField, ""));

        usersCollection.updateMulti(query, update);
    }

    public static void invalidateAllContestResults(Event event, Contest contest) {
        invalidateAllEventResults(event);
        invalidateOneContestResults(event, contest);
    }

    public static void invalidateAllEventResults(Event event) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();
        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());
        DBObject update = new BasicDBObject("$unset", new BasicDBObject(User.FIELD_EVENT_RESULTS, ""));

        usersCollection.updateMulti(query, update);
    }

    public static void invalidateAllResults(Event event) {
        for (Contest contest : event.getContests())
            invalidateOneContestResults(event, contest);
        invalidateAllEventResults(event);
    }

    public static void removeUser(Event event, String login) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject remove = new BasicDBObject(FIELD_EVENT, event.getId());
        remove.put(FIELD_LOGIN, login);

        usersCollection.remove(remove);
    }

}


/*
Группа помощи задержанным: http://vk.com/miting_help▼ , телефон 9878231.
В случае задержания необходимо сообщить информацию о себе, о том, сколько человек в автозаке, куда вас везут и т. д.
Если вы хотите помочь задержанным, также обращайтесь в эту группу.

РосУзник, телефон 8 (951) 666-79-28, твиттер @RosUznik.
Адвокаты РосУзника готовы оказывать помощь.
*/