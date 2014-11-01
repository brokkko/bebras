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
import play.Logger;
import play.Play;
import play.cache.Cache;
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
    public static final String FIELD_PARTIAL_REG = "_p_reg";
    public static final String FIELD_ANNOUNCEMENTS = "_ann";

    public static final String FIELD_LOGIN = "login";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PATRONYMIC = "patronymic";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_RAW_PASS = "raw_pass";

    //TODO separate all user fields that are not from User.info

    private static final PasswordGenerator passwordGenerator = new PasswordGenerator();

    private Info info = null;
    private Event event = null; // should be substituted at update
    private String passwordHash;
    private ObjectId id = new ObjectId();

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
    private boolean partialRegistration;
    private boolean wantAnnouncements;

    private Map<String, ContestInfoForUser> contest2info = new HashMap<>();
    private Info eventResults = null;
    private UserActivityEntry userActivityEntry;

    private UserRole role = UserRole.EMPTY;
    private List<ObjectId> registeredBy = null;

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
                throw new IllegalStateException("Deserializing user with nonexistent event " + eventId); //TODO was here with bebras13 once!! Why??
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

        partialRegistration = deserializer.readBoolean(FIELD_PARTIAL_REG, false);
        wantAnnouncements = deserializer.readBoolean(FIELD_ANNOUNCEMENTS, true);

        //read registered by
        try {
            registeredBy = SerializationTypesRegistry.list(ObjectId.class).read(deserializer, FIELD_REGISTERED_BY);
        } catch (Exception e) {
            Logger.info("fixing reg-by value for user " + id + " " + getLogin() + " (" + eventId + ")");
            registeredBy = new ArrayList<>();
            ObjectId superUserId = deserializer.readObjectId(FIELD_REGISTERED_BY);
            while (superUserId != null) {
                registeredBy.add(superUserId);
                User superUser = User.getUserById(superUserId);
                if (superUser == null) //TODO this occurs if superUser was deleted
                    superUserId = null;
                else
                    superUserId = superUser.getRegisteredBy();
            }
            store();
        }

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

    public void setInfo(Info info) {
        this.info = info;
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
            byte[] hash = f.generateSecret(spec).getEncoded(); //TODO I didn't know this was so slow (~100ms)
            return new BigInteger(1, hash).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeySpecException e) {
            return null;
        }
    }

    public static User current() { //TODO cache users
        Map<String, Object> contextArgs = Http.Context.current().args;

        User user = (User) contextArgs.get("user");
        if (user == null) {
            String username = Http.Context.current().request().username();
            if (username == null)
                return null;
            user = getInstance(FIELD_LOGIN, username);

            if (user == null) //this can occur if the user was removed; usually in debugging situations
                return null;

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
        return getInstance(field, value, "_id".equals(field) ? null : Event.current().getId());
    }

    private static String getLoginCacheKey(String eventId, String login) {
        return "user-cache-" + login + "~@-_" + eventId;
    }

    private static String getIdCacheKey(ObjectId id) {
        return "user-cache-" + id;
    }

    public static User getInstance(String field, Object value, String eventId) {
        boolean byLogin = FIELD_LOGIN.equals(field);
        boolean byId = "_id".equals(field);

        if (byLogin) {
            User result = (User) Cache.get(getLoginCacheKey(eventId, (String) value));
            if (result != null)
                return result;
        }

        if (byId) {
            if (value instanceof String)
                value = new ObjectId((String) value);

            User result = (User) Cache.get(getIdCacheKey((ObjectId) value));
            if (result != null)
                return result;
        }

        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject query = new BasicDBObject(field, value);

        if (!"_id".equals(field))
            query.put(FIELD_EVENT, eventId);

        DBObject userObject = usersCollection.findOne(query);
        User result;
        if (userObject == null)
            result = null;
        else
            result = User.deserialize(new MongoDeserializer(userObject));

        if (result != null)
            result.cache();

        return result;
    }

    public static User getUserById(ObjectId id) {
        return getInstance("_id", id, null);
    }

    public static User getUserByLogin(String login) {
        return getInstance(FIELD_LOGIN, login);
    }

    public static User getUserByLogin(String eventId, String login) {
        return getInstance(FIELD_LOGIN, login, eventId);
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

    public static class UsersEnumeration implements Enumeration<User>, AutoCloseable {

        private DBCursor cursor;

        private UsersEnumeration(DBCursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public void close() throws Exception {
            cursor.close();
        }

        @Override
        public boolean hasMoreElements() {
            return cursor.hasNext();
        }

        @Override
        public User nextElement() {
            DBObject userObject = cursor.next();
            ObjectId id = (ObjectId) userObject.get("_id");
            User result = (User) Cache.get(getIdCacheKey(id));
            if (result != null)
                return result;

            result = User.deserialize(new MongoDeserializer(userObject));
            result.cache();

            return result;
        }

        public List<User> readToMemory() {
            ArrayList<User> result = new ArrayList<>();

            try (UsersEnumeration ue = this) {
                while (ue.hasMoreElements())
                    result.add(ue.nextElement());
            } catch (Exception e) {
                Logger.error("Failed to read users into memory", e);
            }

            return result;
        }
    }

    public static UsersEnumeration listUsers(DBObject query) {
        DBCursor usersCursor = MongoConnection.getUsersCollection().find(query).sort(new BasicDBObject(User.FIELD_LOGIN, 1));
        return new UsersEnumeration(usersCursor);
    }

    public static UsersEnumeration listUsers(DBObject query, DBObject sort) {
        DBCursor usersCursor = MongoConnection.getUsersCollection().find(query).sort(sort);
        return new UsersEnumeration(usersCursor);
    }

    public static String generatePassword() {
        return passwordGenerator.generate(6) + passwordGenerator.generateNumber(2);
    }

    public static String getUsernameSessionKey() {
        return "user-" + Event.currentId();
    }

    public static String getSuUsernameSessionKey() {
        return "su-" + Event.currentId();
    }

    public static String getSubstitutedUser() {
        return Http.Context.current().session().get(getSuUsernameSessionKey());
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

    public boolean isPartialRegistration() {
        return partialRegistration;
    }

    public void setPartialRegistration(boolean partialRegistration) {
        this.partialRegistration = partialRegistration;
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

        SerializationTypesRegistry.list(ObjectId.class).write(serializer, FIELD_REGISTERED_BY, registeredBy);
        serializer.write(FIELD_PARTIAL_REG, partialRegistration);
        serializer.write(FIELD_ANNOUNCEMENTS, wantAnnouncements);

        cache();
    }

    private void cache() {
        int expiration = 10 * 60; // 10 minutes
        Cache.set(getLoginCacheKey(event.getId(), getLogin()), this, expiration);
        Cache.set(getIdCacheKey(id), this, expiration);
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
        MongoConnection.getUsersCollection().save(mongoSerializer.getObject()); //TODO error log claims here may a be a problem with a duplicate key
    }

    /*
     [DuplicateKey: {
        "serverUsed" : "db1/10.146.2.4:27017" ,
        "err" : "E11000 duplicate key error index: dces2.users.$login_1_event_id_1  dup key: { : \"kflf\", : \"bebras13\" }" ,
        "code" : 11000 ,
        "n" : 0 ,
        "connectionId" : 3422 ,
        "ok" : 1.0
     }]
     */

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

    public ContestInfoForUser getContestInfoCreateIfNeeded(String contestId) {
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
            if (pid != null && !problem2lastSubmission.containsKey(pid))
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
        Object name = info.get(FIELD_NAME);
        Object patronymic = info.get(FIELD_PATRONYMIC);

        if (name != null && patronymic != null)
            return name + " " + patronymic;

        if (name != null)
            return name.toString();

        return null;
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

    //may return not null even if getRegisteredByUser() returns null, because the user was removed
    public ObjectId getRegisteredBy() {
        return registeredBy == null || registeredBy.size() == 0 ? null : registeredBy.get(0);
    }

    public User getRegisteredByUser() {
        ObjectId byId = getRegisteredBy();
        return byId == null ? null : getUserById(byId);
    }

    public void setRegisteredBy(User user) {
        this.registeredBy = new ArrayList<>();
        if (user != null) {
            this.registeredBy.add(user.getId());
            this.registeredBy.addAll(user.registeredBy);
        }
    }

    public boolean isWantAnnouncements() {
        return wantAnnouncements;
    }

    public void setWantAnnouncements(boolean wantAnnouncements) {
        this.wantAnnouncements = wantAnnouncements;
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
            ConfiguredProblem configuredProblem = problems.get(i);
            Problem problem = configuredProblem.getProblem();

            Submission submission = submissions.get(i);
            if (submission != null) {
                problemsInfo.add(problem.check(submission.getAnswer(), getContestRandSeed(contest.getId()))); //TODO implement remote check, online check
                problemsSettingsInfo.add(configuredProblem.getSettings());
            } else {
                problemsInfo.add(null);
                problemsSettingsInfo.add(null);
            }

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

    public void evaluateAllContestsResults() {
        for (Contest contest : event.getContests())
            getContestResults(contest);
    }

    public Info getEventResults() {
        if (eventResults != null)
            return eventResults;

        Translator resultTranslator = event.getResultTranslator();

        Collection<Contest> contests = event.getContestsAvailableForUser(this);

        List<Info> contestsInfo = new ArrayList<>(contests.size());
        List<Info> contestsSettings = new ArrayList<>(contests.size());

        for (Contest contest : contests) {
//            if (contest.isAllowRestart()) //TODO invent some method to exclude test contests
//                continue;

            contestsInfo.add(getContestResults(contest));
            contestsSettings.add(null);
        }

        eventResults = resultTranslator.translate(contestsInfo, contestsSettings, this);

        store();

        return eventResults;
    }

    //rights

    public boolean hasRight(String right) {
        int tildePos = right.indexOf("~");
        if (tildePos >= 0) {
            String virtualRight = right.substring(tildePos + 1);
            if (!hasVirtualRights(virtualRight.split("~")))
                return false;

            right = right.substring(0, tildePos);
        }

        return role.hasRight(right);
    }

    private boolean hasVirtualRights(String... rights) {
        for (String right : rights)
            if (!hasVirtualRight(right))
                return false;
        return true;
    }

    private boolean hasVirtualRight(String right) {
        //virtual right may be of form id=value
        String[] fieldAndValue = right.split("=");
        if (fieldAndValue.length != 2)
            return false;
        String field = fieldAndValue[0].trim();
        String value = fieldAndValue[1].trim();

        Object realValue = getInfo().get(field);

        return realValue != null && realValue.toString().equals(value);
    }

    public boolean hasEventAdminRight() {
        return hasRight("event admin");
    }

    private void invalidateEventResults() {
        cachedAllSubmissions.clear();
        if (eventResults != null) {
            eventResults = null;
            store();
        }
    }

    public void invalidateContestResults(String contestId) {
        ContestInfoForUser contestInfo = getContestInfoCreateIfNeeded(contestId);
        if (contestInfo.getFinalResults() != null) {
            contestInfo.setFinalResults(null);
            store();
        }
        invalidateEventResults();
    }

    public void invalidateAllResults() {
        for (Contest contest : event.getContests())
            getContestInfoCreateIfNeeded(contest.getId()).setFinalResults(null);
        invalidateEventResults();
    }

    //TODO move contest and event results out of the user class

    //TODO invalidations for all users do not test clear caches that may have cached users
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

    public static void removeUserById(Event event, ObjectId userId, ObjectId restrictOwner) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject remove = new BasicDBObject(FIELD_EVENT, event.getId());
        remove.put("_id", userId);
        if (restrictOwner != null)
            remove.put(User.FIELD_REGISTERED_BY, restrictOwner);

        String idCacheKey = getIdCacheKey(userId);

        User user = (User) Cache.get(idCacheKey);

        if (user != null)
            Cache.remove(getLoginCacheKey(event.getId(), user.getLogin()));

        Cache.remove(idCacheKey);

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