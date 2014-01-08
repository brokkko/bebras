package plugins;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.Contest;
import models.Event;
import models.User;
import models.data.features.UserApplicationsFeatures;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.certificates.BebrasCertificate;
import plugins.certificates.CertificateLine;
import views.Menu;
import views.html.message;

import java.io.File;
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

    private String gradesDescription; //TODO implement in the other way

    @Override
    public void initPage() {
        Menu.addMenuItem("Мой сертификат", getCall("show_pdf"), "participant");
        Menu.addMenuItem("Мой сертификат", getCall("show_pdf"), "school org");
    }

    @Override
    public void initEvent(Event event) {
    }

    @Override
    public Result doGet(String action, String params) {

        if (action.equals("show_pdf"))
            try {
                return showPdf(params);
            } catch (Exception e) {
                return Results.forbidden("Этот сертификат вам недоступен");
            }

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

    private int getUsersScores(User u) {
        Event event = u.getEvent();
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
        gradesDescription = deserializer.readString("grades description", "");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("region field", regionField);
        serializer.write("russia field", russiaField);
        serializer.write("role", roleName);
        serializer.write("grades description", gradesDescription);
    }

    private Result showPdf(String userLogin) {
        if (userLogin == null || userLogin.isEmpty())
            userLogin = User.current().getLogin();

        Event event = Event.current();
        User user = User.getUserByLogin(userLogin);

        if (user == null)
            return Results.notFound();

        List<CertificateLine> lines;
        boolean isOrg;

        if (user.getRole().getName().equals("SCHOOL_ORG")) {
            int numberOfParticipants = UserApplicationsFeatures.numberOfPayedParticipants(user, "b") + UserApplicationsFeatures.numberOfPayedParticipants(user, "bk");
            int need = 20;
            if (gradesDescription.contains("need10"))
                need = 10;

            Logger.info("number of participants " + numberOfParticipants);

            if (numberOfParticipants == 0)
                return Results.ok(message.render("Сертификат недоступен", "К сожалению, ваш сертификат недоступен. Для получения сертификата необходимо привести хотя бы одного участника.", new String[0]));

            lines = getCertificateLinesForOrg(user, numberOfParticipants >= need);
            isOrg = true;
        } else {
            lines = getCertificateLinesForParticipant(event, user);
            isOrg = false;
        }

        BebrasCertificate certificate = new BebrasCertificate(user, isOrg, lines);
        File pdf = certificate.createPdf();

        Controller.response().setHeader("Content-Disposition", "attachment; filename=bebras-certificate-" + user.getLogin() + ".pdf");
        return Results.ok(pdf);
    }

    private List<CertificateLine> getCertificateLinesForOrg(User user, boolean active) {
        Info info = user.getInfo();

        List<CertificateLine> lines = new ArrayList<>();

        lines.add(new CertificateLine("Настоящим сертификатом удостоверяется, что", 12, false));
        lines.add(new CertificateLine(info.get("surname") + " " + info.get("name") + " " + info.get("patronymic"), 12, true));
        addSchoolAndAddr(lines, info);
        if (active)
            lines.add(new CertificateLine("принял(а) активное участие в подготовке", 12, false));
        else
            lines.add(new CertificateLine("принял(а) участие в подготовке", 12, false));
        lines.add(new CertificateLine("и проведении конкурса «Бобёр-2013»", 12, false));

        return lines;
    }

    private List<CertificateLine> getCertificateLinesForParticipant(Event event, User user) {
        int scores = getUsersScores(user);
        Info info = user.getInfo();
        String grade = (String) info.get("grade");

        String[] descriptions = gradesDescription.split(" ");

        int s1 = -1;
        int s2 = -1;
        int s3 = -1;

        for (int i = 0; i < descriptions.length; i++) {
            if (descriptions[i].equals("g" + grade)) {
                s1 = Integer.parseInt(descriptions[i + 1]);
                s2 = Integer.parseInt(descriptions[i + 2]);
                s3 = Integer.parseInt(descriptions[i + 3]);
                break;
            }
        }

        if (s1 == -1 || s2 == -1 || s3 == -1)
            throw new IllegalArgumentException("Wrong grades description: " + gradesDescription);

        /*
    "grades description" : "g1 45 30 10 g2 45 30 10 g3 85 60 20 g4 85 60 20 g5 75 60 20 g6 75 60 20 g7 75 60 20 g8 75 60 20 g9 75 60 20 g10 75 60 20 g11 75 60 20",
         */

        List<CertificateLine> lines = new ArrayList<>();

        User org = user.getRegisteredByUser();
        Info orgInfo = org.getInfo();

        Contest contest = event.getContestsAvailableForUser(user).get(0);
        long totalParticipants = contest.getNumStarted("PARTICIPANT");

        //get greaterOrEqualParticipants
        DBObject placeQuery = new BasicDBObject();
        placeQuery.put("event_id", event.getId());
        placeQuery.put("grade", grade);
        placeQuery.put("_role", "PARTICIPANT");
        placeQuery.put("_contests." + contest.getId() + ".res.scores", new BasicDBObject("$gte", scores));
        long better = MongoConnection.getUsersCollection().count(placeQuery);

        int percents = (int) Math.ceil(better * 100.0 / totalParticipants);
        if (percents == 0)
            percents = 1;

        if (scores >= s1) {
            lines.add(new CertificateLine("Настоящим сертификатом-дипломом", 12, false));
            lines.add(new CertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new CertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo);
            lines.add(new CertificateLine("получил(а) отличные результаты,", 12, false));
            lines.add(new CertificateLine("участвуя в конкурсе «Бобёр-2013»", 12, false));
            lines.add(new CertificateLine("и вошёл (вошла) в " + percents + "% лучших участников по России", 12, false));
            lines.add(new CertificateLine("(всего " + totalParticipants + " участников " + grade + " класса)", 12, false));
        } else if (scores >= s2) {
            lines.add(new CertificateLine("Настоящим сертификатом-дипломом", 12, false));
            lines.add(new CertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new CertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo);
            lines.add(new CertificateLine("получил(а) хорошие результаты,", 12, false));
            lines.add(new CertificateLine("участвуя в конкурсе «Бобёр-2013»", 12, false));
            lines.add(new CertificateLine("и вошёл (вошла) в " + percents + "% лучших участников по России", 12, false));
            lines.add(new CertificateLine("(всего " + totalParticipants + " участников " + grade + " класса)", 12, false));
        } else if (scores >= s3) {
            lines.add(new CertificateLine("Настоящим сертификатом-грамотой", 12, false));
            lines.add(new CertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new CertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo);
            lines.add(new CertificateLine("успешно участвовал(а) в конкурсе «Бобёр-2013»", 12, false));
            lines.add(new CertificateLine("и вошёл (вошла) в " + percents + "% лучших участников по России", 12, false));
            lines.add(new CertificateLine("(всего " + totalParticipants + " участников " + grade + " класса)", 12, false));
        } else {
            lines.add(new CertificateLine("Настоящим сертификатом", 12, false));
            lines.add(new CertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new CertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo);
            lines.add(new CertificateLine("участвовал(а) в конкурсе «Бобёр-2013»", 12, false));
        }
        return lines;
    }

    private void addSchoolAndAddr(List<CertificateLine> lines, Info orgInfo) {
        String schoolName = (String) orgInfo.get("school_name");
        addProbablyLongLine(lines, schoolName);

        String address = "(" + orgInfo.get("address") + ")";
        addProbablyLongLine(lines, address);
    }

    private void addProbablyLongLine(List<CertificateLine> lines, String line) {
        int len = line.length();
        if (len < 50) {
            lines.add(new CertificateLine(line, 10, false));
            return;
        }

        int i1 = line.indexOf(" ", len / 2); //second half
        int i2 = line.lastIndexOf(" ", len / 2); //first half

        if (i1 < 0 && i2 < 0) {
            lines.add(new CertificateLine(line, 10, false));
            return;
        }

        int i;

        if (i1 < 0)
            i = i2;
        else if (i2 < 0)
            i = i1;
        else if (len / 2 - i2 < i1 - len / 2)
            i = i2;
        else
            i = i1;

        lines.add(new CertificateLine(line.substring(0, i), 10, false));
        lines.add(new CertificateLine(line.substring(i + 1), 10, false));
    }
}
