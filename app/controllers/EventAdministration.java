package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.actions.Authenticated;
import controllers.actions.LoadEvent;
import models.*;
import models.data.CsvDataWriter;
import models.data.Feature;
import models.data.features.ContestResultFeatures;
import models.data.features.FunctionFeature;
import models.forms.InputField;
import models.forms.inputtemplate.AddressInputTemplate;
import models.forms.validators.KenguruSchoolCodeValidator;
import models.problems.Problem;
import models.problems.RootProblemSource;
import models.serialization.JSONSerializer;
import models.serialization.MongoDeserializer;
import play.Logger;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.error;
import views.html.event_admin;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 15.05.13
 * Time: 0:07
 */
@LoadEvent
@Authenticated
public class EventAdministration extends Controller {

    public static Result getUsers(final String eventId, final Integer appendResults) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        final Event currentEvent = Event.current();

        String fileNameTemp = eventId + "-users";
        if (appendResults == 1)
            fileNameTemp += "-ans";
        if (appendResults == 2)
            fileNameTemp += "-ans-2";
        final String fileName = fileNameTemp;

        F.Promise<byte[]> promiseOfInt = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws IOException {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);
                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));

                        writeUsersInfo(currentEvent, zos, appendResults);

                        zos.close();
                        return baos.toByteArray();
                    }
                }
        );
        return async(
                promiseOfInt.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    public static Result getUsersAnswers(final String eventId) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        final Event currentEvent = Event.current();

        final String fileName = eventId + "-answers-to-problems";

        F.Promise<byte[]> promiseOfInt = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws IOException {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);
                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));

                        writeUsersAnswers(currentEvent, zos);

                        zos.close();
                        return baos.toByteArray();
                    }
                }
        );
        return async(
                promiseOfInt.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    //TODO generalize this three get methods
    public static Result getUsersActivity(final String eventId) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        final Event currentEvent = Event.current();

        final String fileName = eventId + "-activity";

        F.Promise<byte[]> promiseOfInt = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws IOException {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);
                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));

                        writeUsersActivity(currentEvent, zos);

                        zos.close();
                        return baos.toByteArray();
                    }
                }
        );
        return async(
                promiseOfInt.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    private static void writeUsersActivity(Event currentEvent, ZipOutputStream zos) {
        DBCollection activityCollection = MongoConnection.getActivityCollection();
        DBObject sort = new BasicDBObject("u", 1);
        sort.put("d", 1);

        try (
                CsvDataWriter<UserActivityEntry> activityWriter = new CsvDataWriter<>(zos);
                DBCursor activityCursor = activityCollection.find().sort(sort)
        ) {
            activityWriter.addFeature(new Feature<UserActivityEntry>() {
                @Override
                public String name() {
                    return "user";
                }

                @Override
                public String eval(UserActivityEntry entry) {
                    return entry.getUser();
                }
            });
            activityWriter.addFeature(new Feature<UserActivityEntry>() {
                @Override
                public String name() {
                    return "ip";
                }

                @Override
                public String eval(UserActivityEntry entry) {
                    return entry.getIp();
                }
            });
            activityWriter.addFeature(new Feature<UserActivityEntry>() {
                @Override
                public String name() {
                    return "user agent";
                }

                @Override
                public String eval(UserActivityEntry entry) {
                    return entry.getUa();
                }
            });
            activityWriter.addFeature(new Feature<UserActivityEntry>() {
                @Override
                public String name() {
                    return "time";
                }

                @Override
                public String eval(UserActivityEntry entry) {
                    return entry.getDate().toString();
                }
            });

            while (activityCursor.hasNext())
                activityWriter.writeObject(UserActivityEntry.deserialize(new MongoDeserializer(activityCursor.next())));


        } catch (Exception e) {
            Logger.error("failed to write user activity");
        }
    }

    private static void writeUsersAnswers(Event currentEvent, ZipOutputStream zos) {
        try (
                CsvDataWriter<Submission> answersWriter = new CsvDataWriter<>(zos)
        ) {
            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "user";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getUserId();
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "contest_id";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getContest().getId();
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "problem_id";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getProblemId();
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "answer";
                }

                @Override
                public String eval(Submission submission) {
                    return submissionToAnswer(submission);
                }
            });

            for (Contest contest : currentEvent.getContests()) {
                DBCollection contestAnswersCollection = contest.getCollection();

                DBObject sort = new BasicDBObject("u", 1);
                sort.put("pid", 1);
                sort.put("lt", -1);

                try (
                        DBCursor answersCursor = contestAnswersCollection.find().sort(sort)
                ) {
                    Submission previousSubmission = null;

                    //write all users
                    while (answersCursor.hasNext()) {
                        Submission submission = new Submission(contest, new MongoDeserializer(answersCursor.next()));

                        if (previousSubmission != null &&
                                previousSubmission.getUserId().equals(submission.getUserId()) &&
                                previousSubmission.getProblemId().equals(submission.getProblemId())
                                ) continue;

                        previousSubmission = submission;

                        answersWriter.writeObject(submission);
                    }

                }
            }
        } catch (Exception e) {
            Logger.error("Failed to write users answers", e);
        }
    }

    public static Result uploadKenguruSchoolCodes(String eventId) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart kenguruCodes = body.getFile("kenguru-codes");
        if (kenguruCodes == null)
            return badRequest(error.render("Не выбран файл для загрузки", new String[]{}));

//            String fileName = kenguruCodes.getFilename();
//            String contentType = kenguruCodes.getContentType();
        try {
            File file = kenguruCodes.getFile();
            Path destPath = Paths.get(KenguruSchoolCodeValidator.getKenguruSchoolsFile().toURI());
            Files.move(Paths.get(file.toURI()), destPath, StandardCopyOption.REPLACE_EXISTING);
            KenguruSchoolCodeValidator.reloadSchoolCodes();
            return redirect(routes.EventAdministration.admin(Event.currentId()));
        } catch (IOException e) {
            Logger.error("Failed to make a file operation", e);
            return badRequest(error.render("Ошибка при загрузке файла, " + e.getMessage(), new String[]{}));
        }
    }

    //TODO report private methods are visible from routes file
    private static void writeUsersInfo(Event event, OutputStream zos, int appendResults) throws IOException {
        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());

        try (
                DBCursor usersCursor = usersCollection.find(query);
                CsvDataWriter<User> userWriter = new CsvDataWriter<>(zos)
        ) {
            userWriter.addFeature(new Feature<User>() {
                @Override
                public String name() {
                    return "id";
                }

                @Override
                public String eval(User user) {
                    return user.getId();
                }
            });

            for (final InputField inputField : event.getUsersForm().getFields())
                if (inputField.getInputTemplate() instanceof AddressInputTemplate) { //TODO this is *** address
                    userWriter.addFeature(new Feature<User>() {
                        @Override
                        public String name() {
                            return "address index";
                        }

                        @Override
                        public String eval(User user) {
                            Address value = (Address) user.get(inputField.getName());
                            return value == null ? "" : value.getIndex();
                        }
                    });
                    userWriter.addFeature(new Feature<User>() {
                        @Override
                        public String name() {
                            return "address city";
                        }

                        @Override
                        public String eval(User user) {
                            Address value = (Address) user.get(inputField.getName());
                            return value == null ? "" : value.getCity();
                        }
                    });
                    userWriter.addFeature(new Feature<User>() {
                        @Override
                        public String name() {
                            return "address street";
                        }

                        @Override
                        public String eval(User user) {
                            Address value = (Address) user.get(inputField.getName());
                            return value == null ? "" : value.getStreet();
                        }
                    });
                    userWriter.addFeature(new Feature<User>() {
                        @Override
                        public String name() {
                            return "address house";
                        }

                        @Override
                        public String eval(User user) {
                            Address value = (Address) user.get(inputField.getName());
                            return value == null ? "" : value.getHouse();
                        }
                    });
                } else
                    userWriter.addFeature(new Feature<User>() {
                        @Override
                        public String name() {
                            return inputField.getName();
                        }

                        @Override
                        public String eval(User user) {
                            Object value = user.get(inputField.getName());
                            return value == null ? "" : value.toString();
                        }
                    });

            Collection<Contest> contests = event.getContests();
            //noinspection unchecked
            List<Feature<User>> contestScoresFeatures = new ArrayList<>();

            for (final Contest contest : contests) {
                userWriter.addFeature(new Feature<User>() {
                    @Override
                    public String name() {
                        return "start of contest \"" + contest.getId() + "\"";
                    }

                    @Override
                    public String eval(User user) {
                        Date start = user.contestStartTime(contest.getId());
                        return start == null ? "" : start.toString();
                    }
                });

                if (appendResults > 0) {
                    ContestResultFeatures contestResultFeatures = new ContestResultFeatures(contest);
                    userWriter.addFeature(contestResultFeatures.getLastSubmissionTimeFeature());

                    userWriter.addFeature(contestResultFeatures.getNumRightFeature());
                    userWriter.addFeature(contestResultFeatures.getNumWrongFeature());
                    userWriter.addFeature(contestResultFeatures.getNumSkippedFeature());

                    Feature<User> scoresFeature = contestResultFeatures.getScoresFeature();
                    if (!contest.isAllowRestart())
                        contestScoresFeatures.add(scoresFeature);

                    userWriter.addFeature(scoresFeature);

                    if (appendResults == 1)
                        contestResultFeatures.appendProblemsFeatures(userWriter);
                    else
                        contestResultFeatures.appendOrderedProblemsFeatures(userWriter);

                    userWriter.addFeature(contestResultFeatures.getProblemsOrderFeature());
                    userWriter.addFeature(contestResultFeatures.getUserHistoryFeature());
                }
            }

            if (appendResults > 0)
                userWriter.addFeature(new FunctionFeature<User>(contestScoresFeatures) {
                    @Override
                    protected String evalFunction(String[] params) {
                        int sum = 0;
                        for (String param : params)
                            sum += Integer.parseInt(param);
                        return "" + sum;
                    }

                    @Override
                    public String name() {
                        return "sum_scores";
                    }
                });

            //write all users
            while (usersCursor.hasNext()) {
                User user = new User();
                user.update(new MongoDeserializer(usersCursor.next()));
                userWriter.writeObject(user);
            }
        } catch (Exception e) {
            Logger.error("Failed to write user info", e);
        }
    }

    public static Result admin(String event) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        return ok(event_admin.render());
    }

    public static Result modifyAdmin(String event, String login, Boolean remove) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        User user = User.getInstance(User.FIELD_LOGIN, login);

        if (user == null)
            return notFound("No such user");

        user.setType(remove ? UserType.PARTICIPANT : UserType.EVENT_ADMIN);
        user.store();

        return ok("admin added");
    }

    public static Result exportTopUsers(String eventId, final String user) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        final Event currentEvent = Event.current();

        final String fileName = eventId + "-user-" + user;

        F.Promise<byte[]> promiseOfInt = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws IOException {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);
                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));

                        writeTopUsers(currentEvent, user, zos);

                        zos.close();
                        return baos.toByteArray();
                    }
                }
        );
        return async(
                promiseOfInt.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    private static void writeTopUsers(Event event, String user, OutputStream zos) {
        try (
                CsvDataWriter<Submission> answersWriter = new CsvDataWriter<>(zos)
        ) {
            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "user";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getUserId();
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "contest_id";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getContest().getId();
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "problem_id";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getProblemId();
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "answer";
                }

                @Override
                public String eval(Submission submission) {
                    //TODO generalize, other answers are different
                    return submissionToAnswer(submission);
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "local_time";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getLocalTime() + "";
                }
            });

            answersWriter.addFeature(new Feature<Submission>() {
                @Override
                public String name() {
                    return "server_time";
                }

                @Override
                public String eval(Submission submission) {
                    return submission.getServerTime() + "";
                }
            });

            for (Contest contest : event.getContests()) {
                DBCollection contestAnswersCollection = contest.getCollection();

                DBObject query = new BasicDBObject();
                if (user.equals("top")) {
                    List<String> topList = Arrays.asList(
                            "51858a0a0cf286004fd80dc6",
                            "5173e9060cf286004fd80cfb",
                            "5169fe9f0cf286004fd80c7c",
                            "518293200cf286004fd80db0",
                            "51879a040cf286004fd80dd2",
                            "515c77e90cf286004fd80b8b",
                            "516025120cf286004fd80be2",
                            "5163ce130cf286004fd80c19",
                            "519651cce4b0eea7c99c0317",
                            "5166e6e30cf286004fd80c4e",
                            "517e44d20cf286004fd80d7d",
                            "519a5230e4b0eea7c99cb235",
                            "5168500f0cf286004fd80c64",
                            "5190c177e4b0219bc1fb822c",
                            "5191fcc4e4b01f79dccd70f2",
                            "516c1a490cf286004fd80c84",
                            "519268b6e4b0f0ff542ee775",
                            "5190c24de4b0219bc1fb8280",
                            "51932b54e4b0f0ff542ef89b",
                            "518aa5110cf2ed7773418cb6",
                            "519a024de4b0eea7c99c90f0",
                            "519a7345e4b0eea7c99cc8d4",
                            "515c5f050cf286004fd80b82",
                            "5197bebae4b0eea7c99c3d48",
                            "51912210e4b0219bc1fbb2d1",
                            "51656c090cf286004fd80c33",
                            "5161c5790cf286004fd80bf4",
                            "5197b6fbe4b0eea7c99c392a",
                            "519a62cbe4b0eea7c99cbf3b",
                            "51988abce4b0eea7c99c4e77"
                    );
                    DBObject userInfo = new BasicDBObject("$in", topList);
                    query.put("u", userInfo);
                } else
                    query.put("u", user);

                DBObject sort = new BasicDBObject("u", 1);
                sort.put("lt", 1);

                try (
                        DBCursor answersCursor = contestAnswersCollection.find(query).sort(sort)
                ) {
                    //write all users
                    while (answersCursor.hasNext()) {
                        Submission submission = new Submission(contest, new MongoDeserializer(answersCursor.next()));
                        answersWriter.writeObject(submission);
                    }

                }
            }
        } catch (Exception e) {
            Logger.error("Failed to write users answers", e);
        }
    }

    public static Result getUsersMatrix(final String eventId) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        final Event currentEvent = Event.current();

        final String fileName = eventId + "-users-matrix";

        F.Promise<byte[]> promiseOfInt = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws IOException {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);
                        zos.putNextEntry(new ZipEntry(fileName + ".mat"));

                        writeUsersMatrix(fileName, currentEvent, zos);

                        zos.close();
                        return baos.toByteArray();
                    }
                }
        );
        return async(
                promiseOfInt.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    private static void writeUsersMatrix(String fileName, Event event, ZipOutputStream zos) throws IOException {
        DBObject allUsersQuery = new BasicDBObject(User.FIELD_EVENT, event.getId());

        List<User> allUsers = new ArrayList<>();

        try (DBCursor allUsersCursor = MongoConnection.getUsersCollection().find(allUsersQuery)) {
            while (allUsersCursor.hasNext())
                allUsers.add(User.deserialize(new MongoDeserializer(allUsersCursor.next())));
        }

        Map<User, String> user2answersList = new HashMap<>();

        int usersCount = 0;
        for (User user : allUsers) {
            Logger.info("reading answers for user #" + (usersCount++) + ": " + user.getLogin());

            StringBuilder userAnswers = new StringBuilder();

            for (Contest contest : event.getContests()) {
                if (contest.isAllowRestart())
                    continue;

                List<String> possibleProblems = contest.getAllPossibleProblems();
                List<Submission> submissions = user.getSubmissionsForContest(contest);

                for (String possibleProblem : possibleProblems) {
                    String ans = "-";
                    //try to find answer in submissions
                    for (Submission submission : submissions)
                        if (submission != null && submission.getProblemId().equals('/' + possibleProblem)) {//TODO code duplication
                            ans = submissionToAnswer(submission);
                            break;
                        }

                    userAnswers.append(ans);
                }
            }

            user2answersList.put(user, userAnswers.toString());
        }

        PrintStream out = new PrintStream(zos);

        out.println(allUsers.size());

        for (User userI : allUsers) {
            String ans1 = user2answersList.get(userI);
            for (User userJ : allUsers) {
                String ans2 = user2answersList.get(userJ);

                int cnt = 0;
                for (int i = 0; i < ans1.length(); i++)
                    if (ans1.charAt(i) != ans2.charAt(i))
                        cnt++;

                out.print(cnt);
                out.print(' ');
            }

            out.println();
        }

        out.flush();
        zos.closeEntry();

        zos.putNextEntry(new ZipEntry(fileName + ".mat.rlabel"));
        out = new PrintStream(zos);
        for (User user : allUsers)
            out.println(user.getId());
        out.flush();
        zos.closeEntry();

        zos.putNextEntry(new ZipEntry(fileName + ".mat.clabel"));
        out = new PrintStream(zos);
        for (User user : allUsers)
            out.println(user.getLogin());
        out.flush();
        zos.closeEntry();
    }


    //TODO generalize, other answers are different
    public static String submissionToAnswer(Submission submission) {
        if (submission == null)
            return "-";

        Integer ansInt = (Integer) submission.getAnswer().get("a");
        if (ansInt < 0)
            return "-";

        Problem problem = RootProblemSource.getInstance().get(submission.getProblemId().substring(1));

        JSONSerializer jsonSerializer = new JSONSerializer();
        problem.check(submission.getAnswer(), jsonSerializer);
        int res = jsonSerializer.getNode().get("result").getIntValue(); //TODO generalize this all

        if (res < 0)
            return (char)(ansInt + 'a') + "";
        else
            return (char)(ansInt + 'A') + "";
    }

    //TODO this is only for BBTC :(
    public static Result evalScores(final String event) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        F.Promise<Boolean> promiseOfVoid = Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() throws IOException {
                        return addScoresForAll(Event.getInstance(event));
                    }
                }
        );
        return async(
                promiseOfVoid.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean v) {
                                return ok("scores filled");
                            }
                        }
                )
        );
    }

    private static Boolean addScoresForAll(Event event) {
        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());

        try(DBCursor cursor = MongoConnection.getUsersCollection().find(query)) {
            while (cursor.hasNext()) {
                User user = User.deserialize(new MongoDeserializer(cursor.next()));
                user.put("__bbtc__scores__", user.totalScores(event));
                user.store();
            }
        }

        return false; //TODO report, suggests importing java.lang.Boolean in "return Boolean"
    }
}