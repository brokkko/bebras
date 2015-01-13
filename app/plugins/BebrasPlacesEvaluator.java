package plugins;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import controllers.worker.Worker;
import models.Contest;
import models.Event;
import models.User;
import models.UserRole;
import models.applications.Application;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.utils.Utils;
import org.bson.types.ObjectId;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.certificates.bebras.BebrasAddressCertificate;
import plugins.certificates.bebras.BebrasCertificate;
import plugins.certificates.bebras.BebrasCertificateLine;
import plugins.certificates.bebras.BebrasGramotaCertificate;
import views.Menu;
import views.html.message;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 28.11.13
 * Time: 22:01
 */
public class BebrasPlacesEvaluator extends Plugin { //TODO get rid of this class, all places evaluations should be done in results translators

    private String regionField; //field name to store result in region
    private String russiaField; //field name to store result in russia
    private String roleName; //role of users
    private boolean showCertificates;
    private int year = 2013;

    private String gradesDescription; //TODO implement in the other way

    @Override
    public void initPage() {
        if (showCertificates) {
            Menu.addMenuItem("Мой сертификат", getCall("show_pdf"), "participant");
            Menu.addMenuItem("Мой сертификат", getCall("show_pdf"), "school org");
        }
    }

    @Override
    public void initEvent(Event event) {
    }

    @Override
    public Result doGet(String action, String params) {

        if (showCertificates) {

            if (action.equals("show_pdf"))
                try {
                    return showPdf(Event.current(), params);
                } catch (Exception e) {
                    return Results.forbidden("Этот сертификат вам недоступен");
                }

            if (action.equals("all_teachers_gramotas"))
                return generateAllGramotas();

            if (action.equals("all_pdfs"))
                return generateAllCertificates(params);

            if (action.equals("all_addrs"))
                return generateAllAddresses();

            // localhost:9000/bebras13/eval_places/all_teachers_gramotas_addrs/p
            if (action.equals("all_teachers_gramotas_addrs"))
                return generateAllAddressesForCertificates();

        }

        if (!action.equals("go"))
            return Results.notFound("unknown action");

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
                                Object regByField = userInfo.get(User.FIELD_REGISTERED_BY);
                                ObjectId regBy;

                                if (regByField instanceof ObjectId)
                                    regBy = (ObjectId) regByField;
                                else
                                    regBy = (ObjectId) ((BasicDBList)regByField).get(0);

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

    private Result generateAllCertificates(final String roleName) {
        final Event event = Event.current();
        UserRole role = event.getRole(roleName);

        if (role == UserRole.EMPTY)
            return Results.badRequest("Unknown role");

        final boolean schoolOrg = roleName.equals("SCHOOL_ORG");
        @SuppressWarnings("UnnecessaryLocalVariable") final boolean needBackground = schoolOrg;

        final Worker worker = new Worker("Generate all certificates", "Event=" + event.getId() + " role=" + roleName);
        worker.execute(new Worker.Task() {
            @Override
            public void run() throws Exception {
                DBObject usersQuery = new BasicDBObject(User.FIELD_EVENT, event.getId());
                usersQuery.put(User.FIELD_USER_ROLE, roleName);

                File outputPath = new File(event.getEventDataFolder(), "all-certificates-" + roleName + ".pdf");

                Rectangle documentSize = new Rectangle(
                        Utilities.millimetersToPoints(450), Utilities.millimetersToPoints(320)
                );
                Document doc = new Document(
                        documentSize,
                        0, 0, 0, 0
                );

                PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));

                Image bgImage = null;
                if (needBackground) {
                    try {
                        bgImage = Image.getInstance(event.getEventDataFolder().getAbsolutePath() + "/bg-organizers-all.png");
                    } catch (Exception e) {
                        worker.logError("Failed to read bg", e);
                        return;
                    }
                    bgImage.setAbsolutePosition(0, 0);
                    bgImage.scaleAbsolute(documentSize.getWidth(), documentSize.getHeight());
                }

                doc.open();

                Map<String, Long> startedCache = new HashMap<>();
                Map<String, Long> betterCache = new HashMap<>();

                List<BebrasCertificate> allCertificates = new ArrayList<>();
                int processedUsers = 0;
                try (User.UsersEnumeration usersEnumeration = User.listUsers(usersQuery)) {
                    while (usersEnumeration.hasMoreElements()) {
                        User user = usersEnumeration.nextElement();

                        List<BebrasCertificateLine> lines;
                        boolean isOrg;

                        if (schoolOrg) {
                            int numberOfParticipants = numberOfParticipants(user);
                            if (numberOfParticipants == 0)
                                continue;

                            boolean organizerActive = isOrganizerActive(numberOfParticipants);

                            //test novosibirsk
                            ObjectId registeredBy = user.getRegisteredBy();
                            if (!organizerActive && BebrasCertificate.isNovosibirsk(registeredBy))
                                continue;

                            lines = getCertificateLinesForOrg(user, organizerActive);
                            isOrg = true;
                        } else { //roleName.equals("PARTICIPANT")
                            try { //if we can not parse a user's grade, then she did not participate
                                int grade = Integer.parseInt((String) user.getInfo().get("grade"));
                                if (grade < 1 || grade > 11)
                                    continue;
                            } catch (Exception ignored) { //failed to parse grade
                                continue;
                            }

                            User organizer = user.getRegisteredByUser();
                            if (BebrasCertificate.isNovosibirsk(organizer.getRegisteredBy()))
                                continue;

                            boolean needOnlyGreatAndGoodResults = false;
                            lines = getCertificateLinesForParticipant(event, user, needOnlyGreatAndGoodResults, startedCache, betterCache);

                            if (lines == null)
                                continue;
                            isOrg = false;
                        }

                        //some extra skips

                        BebrasCertificate certificate = new BebrasCertificate(user, isOrg, lines, year);

                        allCertificates.add(certificate);

                        processedUsers++;
                        if (processedUsers == 1 || processedUsers % 200 == 0)
                            worker.logInfo("processed " + processedUsers + " users");
                    }

                    // sort certificates
                    worker.logInfo("sorting");
                    Collections.sort(allCertificates, new Comparator<BebrasCertificate>() {
                        @Override
                        public int compare(BebrasCertificate o1, BebrasCertificate o2) {
                            User u1 = o1.getUser();
                            User u2 = o2.getUser();

                            if (!schoolOrg) {
                                u1 = u1.getRegisteredByUser();
                                u2 = u2.getRegisteredByUser();
                            }

                            String i1 = Application.getCodeForUser(u1);
                            String i2 = Application.getCodeForUser(u2);

                            return i1.compareTo(i2);
                        }
                    });


                    // now draw all certificates
                    processedUsers = 0;
                    worker.logInfo("printing certificates");
                    for (BebrasCertificate certificate : allCertificates) {
                        int position = processedUsers % 6;
                        if (position == 0) {
                            doc.newPage();
                            if (needBackground)
                                doc.add(bgImage);
                        }
                        certificate.draw(writer, position);

                        processedUsers++;
                        if (processedUsers == 1 || processedUsers % 200 == 0)
                            worker.logInfo("processed " + processedUsers + " users");
                    }
                } catch (Exception e) {
                    worker.logError("Exception occurred", e);
                }

                doc.close();

                worker.logInfo("Finished: " + controllers.routes.Resources.returnDataFile(event.getId(), outputPath.getName()));
            }
        });

        return Results.redirect(controllers.routes.EventAdministration.workersList(event.getId()));
    }

    private Result generateAllGramotas() {
        final Event event = Event.current();
        UserRole role = event.getRole("SCHOOL_ORG");

        if (role == UserRole.EMPTY)
            return Results.badRequest("Unknown role");

        final Worker worker = new Worker("Generate all certificates", "Event=" + event.getId() + " role=" + roleName);
        worker.execute(new Worker.Task() {
            @Override
            public void run() throws Exception {
                DBObject usersQuery = new BasicDBObject(User.FIELD_EVENT, event.getId());
                usersQuery.put(User.FIELD_USER_ROLE, "SCHOOL_ORG");

                File outputPath = new File(event.getEventDataFolder(), "all-gramotas.pdf");

                Rectangle documentSize = new Rectangle(
                        Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(297)
                );
                Document doc = new Document(
                        documentSize,
                        0, 0, 0, 0
                );

                PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));

                Image bgImage;
                try {
                    bgImage = Image.getInstance(event.getEventDataFolder().getAbsolutePath() + "/teacher_gramota.png");
                } catch (Exception e) {
                    worker.logError("Failed to read bg", e);
                    return;
                }
                bgImage.setAbsolutePosition(0, 0);
                bgImage.scaleAbsolute(documentSize.getWidth(), documentSize.getHeight());

                doc.open();

                int processedUsers = 0;
                try (User.UsersEnumeration usersEnumeration = User.listUsers(usersQuery, new BasicDBObject("surname", 1))) {
                    while (usersEnumeration.hasMoreElements()) {
                        User user = usersEnumeration.nextElement();

                        int numberOfParticipants = numberOfParticipants(user);
                        if (numberOfParticipants < 20)
                            continue;

                        boolean organizerActive = numberOfParticipants >= 100;

                        //test novosibirsk
//                        ObjectId registeredBy = user.getRegisteredBy();
//                        if (!organizerActive && BebrasCertificate.NOVOSIBIRSK_ID_13.equals(registeredBy))
//                            continue;

                        List<BebrasCertificateLine> lines = new ArrayList<>();//getCertificateLinesForOrg(user, organizerActive);
                        addSchoolAndAddr(lines, user.getInfo(), user);

                        //some extra skips

                        BebrasGramotaCertificate certificate = new BebrasGramotaCertificate(user, organizerActive, lines);

                        doc.newPage();
                        doc.add(bgImage);
                        certificate.draw(writer);

                        processedUsers++;
                        if (processedUsers == 1 || processedUsers % 200 == 0)
                            worker.logInfo("processed " + processedUsers + " users");
                    }
                } catch (Exception e) {
                    worker.logError("Exception occurred", e);
                }

                doc.close();

                worker.logInfo("Finished: " + controllers.routes.Resources.returnDataFile(event.getId(), outputPath.getName()));
            }
        });

        return Results.redirect(controllers.routes.EventAdministration.workersList(event.getId()));
    }

    private Result generateAllAddresses() {
        final Event event = Event.current();
        final String roleName = "SCHOOL_ORG";
        final UserRole role = event.getRole(roleName);

        if (role == UserRole.EMPTY)
            return Results.badRequest("Unknown role");

        final Worker worker = new Worker("Generate all addresses", "Event=" + event.getId() + " role=" + roleName);
        worker.execute(new Worker.Task() {
            @Override
            public void run() throws Exception {
                DBObject usersQuery = new BasicDBObject(User.FIELD_EVENT, event.getId());
                usersQuery.put(User.FIELD_USER_ROLE, roleName);

                User.UsersEnumeration usersEnumeration = User.listUsers(usersQuery);
                List<User> allUsers = usersEnumeration.readToMemory();
                Collections.sort(allUsers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        String s1 = BebrasCertificate.getUserCode(u1, true, year);
                        String s2 = BebrasCertificate.getUserCode(u2, true, year);
                        return s1.compareTo(s2);
                    }
                });

                File outputPath = new File(event.getEventDataFolder(), "all-addresses-" + roleName + ".pdf");

                Document doc = new Document(
                        new Rectangle(
                                Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(297)
                        ),
                        0, 0, 0, 0
                );

                PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));

                doc.open();

                int processedUsers = 0;
                for (User user : allUsers) {
                    if (roleName.equals("SCHOOL_ORG")) {
                        int numberOfParticipants = numberOfParticipants(user);
                        if (numberOfParticipants == 0)
                            continue;

                        boolean organizerActive = isOrganizerActive(numberOfParticipants);

                        //test novosibirsk
                        ObjectId registeredBy = user.getRegisteredBy();
                        if (!organizerActive && BebrasCertificate.isNovosibirsk(registeredBy))
                            continue;
                    }

                    BebrasAddressCertificate certificate = new BebrasAddressCertificate(user);

                    int position = processedUsers % 11;
                    if (position == 0)
                        doc.newPage();
                    certificate.draw(writer, position);

                    processedUsers++;
                    if (processedUsers == 1 || processedUsers % 100 == 0)
                        worker.logInfo("processed " + processedUsers + " users");
                }

                doc.close();

                worker.logInfo("Finished: " + controllers.routes.Resources.returnDataFile(event.getId(), outputPath.getName()));
            }
        });

        return Results.redirect(controllers.routes.EventAdministration.workersList(event.getId()));
    }

    private Result generateAllAddressesForCertificates() {
        final Event event = Event.current();
        final String roleName = "SCHOOL_ORG";
        final UserRole role = event.getRole(roleName);

        if (role == UserRole.EMPTY)
            return Results.badRequest("Unknown role");

        final Worker worker = new Worker("Generate all addresses", "Event=" + event.getId() + " role=" + roleName);
        worker.execute(new Worker.Task() {
            @Override
            public void run() throws Exception {
                DBObject usersQuery = new BasicDBObject(User.FIELD_EVENT, event.getId());
                usersQuery.put(User.FIELD_USER_ROLE, roleName);

                User.UsersEnumeration usersEnumeration = User.listUsers(usersQuery, new BasicDBObject("surname", 1));
                List<User> allUsers = usersEnumeration.readToMemory();

                File outputPath = new File(event.getEventDataFolder(), "all-addresses-for-certificate-" + roleName + ".pdf");

                Document doc = new Document(
                        new Rectangle(
                                Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(297)
                        ),
                        0, 0, 0, 0
                );

                PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));

                doc.open();

                int processedUsers = 0;
                for (User user : allUsers) {
                    int numberOfParticipants = numberOfParticipants(user);
                    if (numberOfParticipants < 20)
                        continue;

                    BebrasAddressCertificate certificate = new BebrasAddressCertificate(user);

                    int position = processedUsers % 11;
                    if (position == 0)
                        doc.newPage();
                    certificate.draw(writer, position);

                    processedUsers++;
                    if (processedUsers == 1 || processedUsers % 100 == 0)
                        worker.logInfo("processed " + processedUsers + " users");
                }

                doc.close();

                worker.logInfo("Finished: " + controllers.routes.Resources.returnDataFile(event.getId(), outputPath.getName()));
            }
        });

        return Results.redirect(controllers.routes.EventAdministration.workersList(event.getId()));
    }

    private int getUsersScores(User u) {
        Event event = u.getEvent();
        int sum = 0;
        List<Contest> contests = event.getContestsAvailableForUser(u);
        for (Contest contest : contests) {
            if (contest.isAvailableForAnon())
                continue;
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
        showCertificates = deserializer.readBoolean("show certificates", false);
        year = deserializer.readInt("year", 2013);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("region field", regionField);
        serializer.write("russia field", russiaField);
        serializer.write("role", roleName);
        serializer.write("grades description", gradesDescription);
        serializer.write("show certificates", showCertificates);
        serializer.write("year", year);
    }

    private Result showPdf(Event event, String userLogin) {
        if (userLogin == null || userLogin.isEmpty())
            userLogin = User.current().getLogin();

        User user = User.getUserByLogin(event.getId(), userLogin);

        if (user == null)
            return Results.notFound();

        List<BebrasCertificateLine> lines;
        boolean isOrg;

        if (user.getRole().getName().equals("SCHOOL_ORG")) {
            int numberOfParticipants = numberOfParticipants(user);
            if (numberOfParticipants == 0)
                return Results.ok(message.render("Сертификат недоступен", "К сожалению, ваш сертификат недоступен. Для получения сертификата необходимо привести хотя бы одного участника.", new String[0]));

            lines = getCertificateLinesForOrg(user, isOrganizerActive(numberOfParticipants));
            isOrg = true;
        } else {
            lines = getCertificateLinesForParticipant(event, user, false, null, null);
            isOrg = false;
        }

        BebrasCertificate certificate = new BebrasCertificate(user, isOrg, lines, year);
        File pdf = certificate.createPdf();

        Controller.response().setHeader("Content-Disposition", "attachment; filename=bebras-certificate-" + user.getLogin() + ".pdf");
        return Results.ok(pdf);
    }

    private int numberOfParticipants(User organizer) {
        if (organizer.getLogin().contains("_helper_")) //only bebras13
            return organizer.getLogin().startsWith("Kondratieva_VV") ? 1 : 200; // helpers

//        return UserApplicationsFeatures.numberOfPayedParticipants(organizer, "b") + UserApplicationsFeatures.numberOfPayedParticipants(organizer, "bk");
        DBObject participantsQuery = new BasicDBObject(User.FIELD_EVENT, organizer.getEvent().getId());
        participantsQuery.put(User.FIELD_USER_ROLE, "PARTICIPANT");
        participantsQuery.put(User.FIELD_REGISTERED_BY, organizer.getId());
        return (int) MongoConnection.getUsersCollection().count(participantsQuery);
    }

    private boolean isOrganizerActive(int numberOfParticipants) {
        int need = 20;
        if (gradesDescription.contains("need10"))
            need = 10;

        return numberOfParticipants >= need;
    }

    private List<BebrasCertificateLine> getCertificateLinesForOrg(User user, boolean active) {
        Info info = user.getInfo();

        List<BebrasCertificateLine> lines = new ArrayList<>();

        lines.add(new BebrasCertificateLine("Настоящим сертификатом удостоверяется, что", 12, false));
        lines.add(new BebrasCertificateLine(info.get("surname") + " " + info.get("name") + " " + info.get("patronymic"), 12, true));
        addSchoolAndAddr(lines, info, user);
        if (active)
            lines.add(new BebrasCertificateLine("принял(а) активное участие в подготовке", 12, false));
        else
            lines.add(new BebrasCertificateLine("принял(а) участие в подготовке", 12, false));
        lines.add(new BebrasCertificateLine("и проведении конкурса «Бобёр-" + year + "»", 12, false));

        return lines;
    }

    private List<BebrasCertificateLine> getCertificateLinesForParticipant(Event event, User user, boolean needOnlyGreatAndGoodResults, Map<String, Long> numStartedCache, Map<String, Long> betterCache) {
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
            throw new IllegalArgumentException("Wrong grades description: " + grade + " < " + gradesDescription);

        /*
        example of grades description
    "grades description" : "g1 45 30 10 g2 45 30 10 g3 85 60 20 g4 85 60 20 g5 75 60 20 g6 75 60 20 g7 75 60 20 g8 75 60 20 g9 75 60 20 g10 75 60 20 g11 75 60 20",
         */

        List<BebrasCertificateLine> lines = new ArrayList<>();

        User org = user.getRegisteredByUser();
        Info orgInfo = org.getInfo();

        Contest contest = getMainContest(event, user);

        //get total participants from cache
        String totalParticipantsCacheKey = contest.getId() + "|" + grade;
        Long totalParticipants = null;
        if (numStartedCache != null)
            totalParticipants = numStartedCache.get(totalParticipantsCacheKey);

        if (totalParticipants == null) {
            totalParticipants = contest.getNumStarted("PARTICIPANT", Utils.mapify("grade", grade));
            if (numStartedCache != null)
                numStartedCache.put(totalParticipantsCacheKey, totalParticipants);
        }

        //get greaterOrEqualParticipants
        long better = getBetter(event, scores, grade, contest, betterCache);

        int percents = (int) Math.ceil(better * 100.0 / totalParticipants);
        if (percents == 0)
            percents = 1;

        /*
        Всего участников этого класса N

        считаем p = X в процентах от N, округляем до целого вверх.

        если X > 50, пишем
            "И вошел в p% лучших участников по России"
        eсли X = 1
            "И занял первое место по России"
        иначе пишем
            "И вошел в X лучших участников по России"
         */

        if (scores >= s1) {
            lines.add(new BebrasCertificateLine("Настоящим сертификатом", 12, false));
            lines.add(new BebrasCertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new BebrasCertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo, user);
            lines.add(new BebrasCertificateLine("получил(а) отличные результаты,", 12, false));
            lines.add(new BebrasCertificateLine("участвуя в конкурсе «Бобёр-" + year + "»", 12, false));
            if (better == 1)
                lines.add(new BebrasCertificateLine("и занял первое место по России", 12, false));
            else if (percents == 1)
                lines.add(new BebrasCertificateLine("и вошёл (вошла) в " + better + " лучших участников по России", 12, false)); //don't write 1%, write instead the whole number
            else
                lines.add(new BebrasCertificateLine("и вошёл (вошла) в " + percents + "% лучших участников по России", 12, false));
            lines.add(new BebrasCertificateLine("(всего участников " + grade + " класса: " + totalParticipants + ")", 12, false));
        } else if (scores >= s2) {
            lines.add(new BebrasCertificateLine("Настоящим сертификатом", 12, false));
            lines.add(new BebrasCertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new BebrasCertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo, user);
            lines.add(new BebrasCertificateLine("получил(а) хорошие результаты,", 12, false));
            lines.add(new BebrasCertificateLine("участвуя в конкурсе «Бобёр-" + year + "»", 12, false));
            lines.add(new BebrasCertificateLine("и вошёл (вошла) в " + percents + "% лучших участников по России", 12, false));
            lines.add(new BebrasCertificateLine("(всего участников " + grade + " класса: " + totalParticipants + ")", 12, false));
        } else if (scores >= s3) {
            if (needOnlyGreatAndGoodResults)
                return null;
            lines.add(new BebrasCertificateLine("Настоящим сертификатом", 12, false));
            lines.add(new BebrasCertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new BebrasCertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo, user);
            lines.add(new BebrasCertificateLine("успешно участвовал(а) в конкурсе «Бобёр-" + year + "»", 12, false));
            lines.add(new BebrasCertificateLine("и вошёл (вошла) в " + percents + "% лучших участников по России", 12, false));
            lines.add(new BebrasCertificateLine("(всего участников " + grade + " класса: " + totalParticipants + ")", 12, false));
        } else {
            if (needOnlyGreatAndGoodResults)
                return null;
            lines.add(new BebrasCertificateLine("Настоящим сертификатом", 12, false));
            lines.add(new BebrasCertificateLine("удостоверяется, что ученик(ца) "  + grade + " класса", 12, false));
            lines.add(new BebrasCertificateLine(info.get("surname") + " " + info.get("name"), 12, true));
            addSchoolAndAddr(lines, orgInfo, user);
            lines.add(new BebrasCertificateLine("участвовал(а) в конкурсе «Бобёр-" + year + "»", 12, false));
        }
        return lines;
    }

    private Contest getMainContest(Event event, User user) {
        List<Contest> allContests = event.getContestsAvailableForUser(user);
        for (Contest contest : allContests)
            if (!contest.isAvailableForAnon())
                return contest;

        return allContests.get(0); //just return something (or null)
    }

    private long getBetter(Event event, int scores, String grade, Contest contest, Map<String, Long> betterCache) {
        Long result = null;
        String cacheKey = null;

        if (betterCache != null) {
            cacheKey = grade + "@" + contest.getId() + "@" + scores;
            result = betterCache.get(cacheKey);
        }

        if (result != null)
            return result;

        DBObject placeQuery = new BasicDBObject();
        placeQuery.put("event_id", event.getId());
        placeQuery.put("grade", grade);
        placeQuery.put("_role", "PARTICIPANT");
        placeQuery.put("_contests." + contest.getId() + ".res.scores", new BasicDBObject("$gte", scores));

        result = MongoConnection.getUsersCollection().count(placeQuery);

        if (betterCache != null)
            betterCache.put(cacheKey, result);

        return result;
    }

    private void addSchoolAndAddr(List<BebrasCertificateLine> lines, Info orgInfo, User user) {
        String schoolName = substituteSchoolBebras13((String) orgInfo.get("school_name"), user);
        addProbablyLongLine(lines, schoolName);

        String orgAddress = (String)orgInfo.get("address_for_certificate");
        if (orgAddress == null || orgAddress.isEmpty())
            orgAddress = (String) orgInfo.get("address");

        String address = substituteAddrBebras13("(" + orgAddress + ")", user);
        if (!"-".equals(orgAddress))
            addProbablyLongLine(lines, address);
    }

    private String substituteSchoolBebras13(String schoolName, User user) {
        if (year != 2013)
            return schoolName;

        switch (user.getLogin()) {
            case "d26cf21.1":
                return "МБОУ \"Пажгинская СОШ\"";
            case "d8a4b71.1":
                return "МБОУ \"СОШ № 5\"";
            case "d8a4b71.2":
                return "МБОУ \"СОШ № 5\"";
            case "c9d6341.1":
                return "МАОУ \"Гимназия №5\"";
        }

        return schoolName;
    }

    private String substituteAddrBebras13(String address, User user) {
        if (year != 2013)
            return address;

        switch (user.getLogin()) {
            case "d26cf21.1":
                return "Республика Коми, Сыктывдинский район, с. Пажга, 1-й микрорайон, д.23";
            case "d8a4b71.1":
                return "г.Усинск ул.Возейская, № 9а";
            case "d8a4b71.2":
                return "г.Усинск ул.Возейская, № 9а";
            case "c9d6341.1":
                return "г. Новороссийк, ул. Цедрика, 7";
        }

        return address;
    }

    private void addProbablyLongLine(List<BebrasCertificateLine> lines, String line) {
        for (String shortLine : splitProbablyLongLine(line))
            lines.add(new BebrasCertificateLine(shortLine, 10, false));
    }

    public static String[] splitProbablyLongLine(String line) {
        int len = line.length();
        if (len < 50)
            return new String[]{line};

        int i1 = line.indexOf(" ", len / 2); //second half
        int i2 = line.lastIndexOf(" ", len / 2); //first half

        if (i1 < 0 && i2 < 0)
            return new String[]{line};

        int i;

        if (i1 < 0)
            i = i2;
        else if (i2 < 0)
            i = i1;
        else if (len / 2 - i2 < i1 - len / 2)
            i = i2;
        else
            i = i1;

        return new String[]{line.substring(0, i), line.substring(i + 1)};
    }
}
