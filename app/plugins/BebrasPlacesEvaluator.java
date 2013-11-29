package plugins;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.Contest;
import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import org.bson.types.ObjectId;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 28.11.13
 * Time: 22:01
 */
public class BebrasPlacesEvaluator extends Plugin {

    private String regionField; //field name to store result in region
    private String russiaField; //field name to store result in russia
    private String roleName; //role of users

    @Override
    public void initPage() {
    }

    @Override
    public void initEvent(Event event) {
    }

    @Override
    public Result doGet(String action, String params) {

        final Event event = Event.current();
        final String eventId = event.getId();

        F.Promise<Boolean> promiseOfVoid = Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        //select all logins from database
                        DBObject query = new BasicDBObject(User.FIELD_EVENT, eventId);
                        query.put(User.FIELD_USER_ROLE, roleName);

                        List<User> users = new ArrayList<>();
                        final Map<ObjectId, String> uid2region = new HashMap<>();

                        BasicDBObject keys = new BasicDBObject(User.FIELD_LOGIN, 1);
                        keys.put(User.FIELD_REGISTERED_BY, 1);

                        try (DBCursor allUsers = MongoConnection.getUsersCollection().find(query, keys)) {
                            while (allUsers.hasNext()) {
                                DBObject userInfo = allUsers.next();
                                String login = (String) userInfo.get(User.FIELD_LOGIN);
                                User user = User.getInstance(User.FIELD_LOGIN, login, eventId);
                                users.add(user);
                                ObjectId regBy = (ObjectId) userInfo.get(User.FIELD_REGISTERED_BY);
                                String region = uid2region.get(regBy);
                                if (region == null) {
                                    User teacher = User.getUserById(regBy);
                                    region = (String) teacher.getInfo().get("region");
                                    uid2region.put(regBy, region);
                                }
                            }
                        }

                        //set places for russia

                        Collections.sort(users, new UserComparator(false, uid2region));

                        int wasGrade = 0;
                        int wasScores = 0;
                        int wasPlace = 0;
                        int userIndex = 0;
                        for (User user : users) {
                            int grade = getUserGrade(user);
                            if (grade == 0)
                                continue;

                            if (grade != wasGrade) {
                                wasPlace = 1;
                                userIndex = 1;
                                wasScores = getUsersScores(user);
                                user.getInfo().put(russiaField, wasPlace + "");
                                wasGrade = grade;
                                user.store();
                                continue;
                            }

                            userIndex ++;
                            int nowScores = getUsersScores(user);
                            if (nowScores != wasScores) {
                                wasPlace = userIndex;
                                wasScores = nowScores;
                            }

                            user.getInfo().put(russiaField, wasPlace + "");

                            user.store();
                        }

                        //set places for regions

                        Collections.sort(users, new UserComparator(true, uid2region));

                        wasGrade = 0;
                        wasScores = 0;
                        wasPlace = 0;
                        String wasRegion = "";
                        userIndex = 0;
                        for (User user : users) {
                            int grade = getUserGrade(user);
                            if (grade == 0)
                                continue;

                            String nowRegion = uid2region.get(user.getRegisteredBy());

                            if (grade != wasGrade || !wasRegion.equals(nowRegion)) {
                                wasPlace = 1;
                                userIndex = 1;
                                wasScores = getUsersScores(user);
                                user.getInfo().put(regionField, wasPlace + "");
                                wasGrade = grade;
                                wasRegion = nowRegion;
                                user.store();
                                continue;
                            }

                            userIndex ++;
                            int nowScores = getUsersScores(user);
                            if (nowScores != wasScores) {
                                wasPlace = userIndex;
                                wasScores = nowScores;
                            }

                            user.getInfo().put(regionField, wasPlace + "");

                            user.store();
                        }

                        return true;
                    }

                    private int getUsersScores(User u) {
                        int sum = 0;
                        List<Contest> contests = event.getContestsAvailableForUser(u);
                        for (Contest contest : contests) {
                            Info finalResults = u.getContestInfoCreateIfNeeded(contest.getId()).getFinalResults();
                            if (finalResults == null)
                                continue;
                            Integer scores = (Integer) finalResults.get("scores");
                            sum += scores == null ? 0 : scores;
                        }
                        return sum;
                    }

                    private int getUserGrade(User u) {
                        Object gradeO = u.getInfo().get("grade");
                        if (gradeO == null)
                            gradeO = "0";
                        return Integer.parseInt((String) gradeO);
                    }

                    class UserComparator implements Comparator<User> {

                        private boolean compareRegions;
                        private Map<ObjectId, String> uid2region;

                        public UserComparator(boolean compareRegions, Map<ObjectId, String> uid2region) {
                            this.compareRegions = compareRegions;
                            this.uid2region = uid2region;
                        }

                        @Override
                        public int compare(User u1, User u2) {
                            int grade1 = getUserGrade(u1);
                            int grade2 = getUserGrade(u2);

                            if (grade1 < grade2)
                                return -1;
                            if (grade1 > grade2)
                                return 1;

                            if (compareRegions) {
                            String region1 = uid2region.get(u1.getRegisteredBy());
                            String region2 = uid2region.get(u2.getRegisteredBy());

                            int regionCompare = region1.compareTo(region2);
                            if (regionCompare != 0)
                                return regionCompare;
                            }

                            int s1 = getUsersScores(u1);
                            int s2 = getUsersScores(u2);

                            return s2 - s1;
                        }
                    }
                }
        );

        return Results.async(
                promiseOfVoid.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean result) {
                                return Results.ok("finished " + new Date());
                            }
                        }
                )
        );
    }

    @Override
    public Result doPost(String action, String params) {
        return null;
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        regionField = deserializer.readString("region field");
        russiaField = deserializer.readString("russia field");
        roleName = deserializer.readString("role");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("region field", regionField);
        serializer.write("russia field", russiaField);
        serializer.write("role", roleName);
    }
}
