//package controllers;
//
//import au.com.bytecode.opencsv.CSVReader;
//import com.mongodb.BasicDBObject;
//import com.mongodb.DBCollection;
//import com.mongodb.DBCursor;
//import com.mongodb.DBObject;
//import models.*;
//import models.data.CsvDataWriter;
//import models.data.features.ContestResultFeatures;
//import models.newserialization.MongoDeserializer;
//import models.problems.Problem;
//import models.problems.RootProblemSource;
//import models.results.Info;
//import play.Logger;
//import play.libs.Akka;
//import play.libs.F;
//import play.mvc.Controller;
//import play.mvc.Result;
//
//import java.io.*;
//import java.util.*;
//import java.util.concurrent.Callable;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
///**
// * Created with IntelliJ IDEA.
// * User: ilya
// * Date: 24.07.13
// * Time: 22:57
// */
//public class EventAdministrationTrash extends Controller {
//
//    //TODO this is only for BBTC :(
//    public static Result evalScores(final String event) {
//        if (User.current().getType() != UserType.EVENT_ADMIN)
//            return forbidden();
//
//        F.Promise<Boolean> promiseOfVoid = Akka.future(
//                new Callable<Boolean>() {
//                    public Boolean call() throws IOException {
//                        return addScoresForAll(Event.getInstance(event));
//                    }
//                }
//        );
//        return async(
//                promiseOfVoid.map(
//                        new F.Function<Boolean, Result>() {
//                            public Result apply(Boolean v) {
//                                return ok("scores filled");
//                            }
//                        }
//                )
//        );
//    }
//
//    private static Boolean addScoresForAll(Event event) throws IOException {
//        //read all users
//        HashMap<String, String> user2place = new HashMap<>();
//        CSVReader reader = new CSVReader(
//                new BufferedReader(new InputStreamReader(
//                        new FileInputStream(new File(event.getEventDataFolder(), "places.csv")), "windows-1251"
//                )),
//                ';', '"', 1
//        );
//
//        String[] line;
//        while ((line = reader.readNext()) != null)
//            user2place.put(line[0].trim(), line[line.length -1].trim());
//
//        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());
//
//        try(DBCursor cursor = MongoConnection.getUsersCollection().find(query)) {
//            while (cursor.hasNext()) {
//                User user = User.deserialize(new MongoDeserializer(cursor.next()));
//                user.put("__bbtc__scores__", user2place.get(user.getId().toString()));
//                user.store();
//            }
//        }
//
//        return false; //TODO report, suggests importing java.lang.Boolean in "return Boolean"
//    }
//
//    public static Result getUsers(final String eventId, final Integer appendResults) {
//        if (User.current().getType() != UserType.EVENT_ADMIN)
//            return forbidden();
//
//        final Event currentEvent = Event.current();
//
//        String fileNameTemp = eventId + "-users";
//        if (appendResults == 1)
//            fileNameTemp += "-ans";
//        if (appendResults == 2)
//            fileNameTemp += "-ans-2";
//        final String fileName = fileNameTemp;
//
//        F.Promise<byte[]> promiseOfInt = Akka.future(
//                new Callable<byte[]>() {
//                    public byte[] call() throws IOException {
//
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        ZipOutputStream zos = new ZipOutputStream(baos);
//                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));
//
//                        writeUsersInfo(currentEvent, zos, appendResults);
//
//                        zos.close();
//                        return baos.toByteArray();
//                    }
//                }
//        );
//        return async(
//                promiseOfInt.map(
//                        new F.Function<byte[], Result>() {
//                            public Result apply(byte[] file) {
//                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
//                                return ok(file).as("application/zip");
//                            }
//                        }
//                )
//        );
//    }
//
//    public static Result getUsersAnswers(final String eventId) {
//        if (User.current().getType() != UserType.EVENT_ADMIN)
//            return forbidden();
//
//        final Event currentEvent = Event.current();
//
//        final String fileName = eventId + "-answers-to-problems";
//
//        F.Promise<byte[]> promiseOfInt = Akka.future(
//                new Callable<byte[]>() {
//                    public byte[] call() throws IOException {
//
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        ZipOutputStream zos = new ZipOutputStream(baos);
//                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));
//
//                        writeUsersAnswers(currentEvent, zos);
//
//                        zos.close();
//                        return baos.toByteArray();
//                    }
//                }
//        );
//        return async(
//                promiseOfInt.map(
//                        new F.Function<byte[], Result>() {
//                            public Result apply(byte[] file) {
//                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
//                                return ok(file).as("application/zip");
//                            }
//                        }
//                )
//        );
//    }
//
//    public static Result getRegisteredUsers(final String eventId) {
//        if (User.current().getType() != UserType.EVENT_ADMIN)
//            return forbidden();
//
//        final Event currentEvent = Event.current();
//
//        final String fileName = eventId + "-registered-users";
//
//        F.Promise<byte[]> promiseOfInt = Akka.future(
//                new Callable<byte[]>() {
//                    public byte[] call() throws IOException {
//
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        ZipOutputStream zos = new ZipOutputStream(baos);
//                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));
//
//                        writeRegisteredUsersInfo(currentEvent, zos);
//
//                        zos.close();
//                        return baos.toByteArray();
//                    }
//                }
//        );
//        return async(
//                promiseOfInt.map(
//                        new F.Function<byte[], Result>() {
//                            public Result apply(byte[] file) {
//                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
//                                return ok(file).as("application/zip");
//                            }
//                        }
//                )
//        );
//    }
//
//    //TODO generalize this three get methods
//    public static Result getUsersActivity(final String eventId) {
//        if (User.current().getType() != UserType.EVENT_ADMIN)
//            return forbidden();
//
//        final Event currentEvent = Event.current();
//
//        final String fileName = eventId + "-activity";
//
//        F.Promise<byte[]> promiseOfInt = Akka.future(
//                new Callable<byte[]>() {
//                    public byte[] call() throws IOException {
//
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        ZipOutputStream zos = new ZipOutputStream(baos);
//                        zos.putNextEntry(new ZipEntry(fileName + ".csv"));
//
//                        writeUsersActivity(currentEvent, zos);
//
//                        zos.close();
//                        return baos.toByteArray();
//                    }
//                }
//        );
//        return async(
//                promiseOfInt.map(
//                        new F.Function<byte[], Result>() {
//                            public Result apply(byte[] file) {
//                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
//                                return ok(file).as("application/zip");
//                            }
//                        }
//                )
//        );
//    }
//
//    private static void writeUsersActivity(Event currentEvent, ZipOutputStream zos) {
//        DBCollection activityCollection = MongoConnection.getActivityCollection();
//        DBObject sort = new BasicDBObject("u", 1);
//        sort.put("d", 1);
//
//        try (
//                CsvDataWriter<UserActivityEntry> activityWriter = new CsvDataWriter<>(zos);
//                DBCursor activityCursor = activityCollection.find().sort(sort)
//        ) {
//            activityWriter.addFeature(new Feature<UserActivityEntry>() {
//                @Override
//                public String name() {
//                    return "user";
//                }
//
//                @Override
//                public String eval(UserActivityEntry entry) {
//                    return entry.getUser().toString();
//                }
//            });
//            activityWriter.addFeature(new Feature<UserActivityEntry>() {
//                @Override
//                public String name() {
//                    return "ip";
//                }
//
//                @Override
//                public String eval(UserActivityEntry entry) {
//                    return entry.getIp();
//                }
//            });
//            activityWriter.addFeature(new Feature<UserActivityEntry>() {
//                @Override
//                public String name() {
//                    return "user agent";
//                }
//
//                @Override
//                public String eval(UserActivityEntry entry) {
//                    return entry.getUa();
//                }
//            });
//            activityWriter.addFeature(new Feature<UserActivityEntry>() {
//                @Override
//                public String name() {
//                    return "time";
//                }
//
//                @Override
//                public String eval(UserActivityEntry entry) {
//                    return entry.getDate().toString();
//                }
//            });
//
//            while (activityCursor.hasNext())
//                activityWriter.writeObject(UserActivityEntry.deserialize(new MongoDeserializer(activityCursor.next())));
//
//
//        } catch (Exception e) {
//            Logger.error("failed to write user activity");
//        }
//    }
//
//    private static void writeUsersAnswers(Event currentEvent, ZipOutputStream zos) {
//        try (
//                CsvDataWriter<Submission> answersWriter = new CsvDataWriter<>(zos)
//        ) {
//            answersWriter.addFeature(new Feature<Submission>() {
//                @Override
//                public String name() {
//                    return "user";
//                }
//
//                @Override
//                public String eval(Submission submission) {
//                    return submission.getUser().toString();
//                }
//            });
//
//            answersWriter.addFeature(new Feature<Submission>() {
//                @Override
//                public String name() {
//                    return "contest_id";
//                }
//
//                @Override
//                public String eval(Submission submission) {
//                    return submission.getContest().getId();
//                }
//            });
//
//            answersWriter.addFeature(new Feature<Submission>() {
//                @Override
//                public String name() {
//                    return "problem_id";
//                }
//
//                @Override
//                public String eval(Submission submission) {
//                    return submission.getProblemId();
//                }
//            });
//
//            answersWriter.addFeature(new Feature<Submission>() {
//                @Override
//                public String name() {
//                    return "answer";
//                }
//
//                @Override
//                public String eval(Submission submission) {
//                    return submissionToAnswer(submission);
//                }
//            });
//
//            for (Contest contest : currentEvent.getContests()) {
//                DBCollection contestAnswersCollection = contest.getCollection();
//
//                DBObject sort = new BasicDBObject("u", 1);
//                sort.put("pid", 1);
//                sort.put("lt", -1);
//
//                try (
//                        DBCursor answersCursor = contestAnswersCollection.find().sort(sort)
//                ) {
//                    Submission previousSubmission = null;
//
//                    //write all users
//                    while (answersCursor.hasNext()) {
//                        MongoDeserializer submissionDeserializer = new MongoDeserializer(answersCursor.next());
//                        Submission submission = new Submission(contest, submissionDeserializer);
//
//                        if (previousSubmission != null &&
//                                previousSubmission.getUser().equals(submission.getUser()) &&
//                                previousSubmission.getProblemId().equals(submission.getProblemId())
//                                ) continue;
//
//                        previousSubmission = submission;
//
//                        answersWriter.writeObject(submission);
//                    }
//
//                }
//            }
//        } catch (Exception e) {
//            Logger.error("Failed to write users answers", e);
//        }
//    }
//
//    //TODO report private methods are visible from routes file
//    private static void writeUsersInfo(Event event, OutputStream zos, int appendResults) throws IOException {
//        DBCollection usersCollection = MongoConnection.getUsersCollection();
//
//        //TODO move all such queries to user
//        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());
//
//        try (
//                DBCursor usersCursor = usersCollection.find(query);
//                CsvDataWriter<User> userWriter = new CsvDataWriter<>(zos)
//        ) {
//            userWriter.addFeature(new Feature<User>() {
//                @Override
//                public String name() {
//                    return "id";
//                }
//
//                @Override
//                public String eval(User user) {
//                    return user.getId().toString();
//                }
//            });
//
//            //TODO rewrite
//            /*for (final String field : event.getUserInfoPattern().getUserInputFields())
//                if (inputField.getInputTemplate() instanceof AddressInputTemplate) {
//                    userWriter.addFeature(new Feature<User>() {
//                        @Override
//                        public String name() {
//                            return "address index";
//                        }
//
//                        @Override
//                        public String eval(User user) {
//                            Address value = (Address) user.get(inputField.getName());
//                            return value == null ? "" : value.getIndex();
//                        }
//                    });
//                    userWriter.addFeature(new Feature<User>() {
//                        @Override
//                        public String name() {
//                            return "address city";
//                        }
//
//                        @Override
//                        public String eval(User user) {
//                            Address value = (Address) user.get(inputField.getName());
//                            return value == null ? "" : value.getCity();
//                        }
//                    });
//                    userWriter.addFeature(new Feature<User>() {
//                        @Override
//                        public String name() {
//                            return "address street";
//                        }
//
//                        @Override
//                        public String eval(User user) {
//                            Address value = (Address) user.get(inputField.getName());
//                            return value == null ? "" : value.getStreet();
//                        }
//                    });
//                    userWriter.addFeature(new Feature<User>() {
//                        @Override
//                        public String name() {
//                            return "address house";
//                        }
//
//                        @Override
//                        public String eval(User user) {
//                            Address value = (Address) user.get(inputField.getName());
//                            return value == null ? "" : value.getHouse();
//                        }
//                    });
//                } else
//                    userWriter.addFeature(new Feature<User>() {
//                        @Override
//                        public String name() {
//                            return inputField.getName();
//                        }
//
//                        @Override
//                        public String eval(User user) {
//                            Object value = user.get(inputField.getName());
//                            return value == null ? "" : value.toString();
//                        }
//                    });*/
//
//            Collection<Contest> contests = event.getContests();
//            //noinspection unchecked
//            List<Feature<User>> contestScoresFeatures = new ArrayList<>();
//
//            for (final Contest contest : contests) {
//                userWriter.addFeature(new Feature<User>() {
//                    @Override
//                    public String name() {
//                        return "start of contest \"" + contest.getId() + "\"";
//                    }
//
//                    @Override
//                    public String eval(User user) {
//                        Date start = user.contestStartTime(contest.getId());
//                        return start == null ? "" : start.toString();
//                    }
//                });
//
//                if (appendResults > 0) {
//                    ContestResultFeatures contestResultFeatures = new ContestResultFeatures(contest);
//                    userWriter.addFeature(contestResultFeatures.getLastSubmissionTimeFeature());
//
//                    userWriter.addFeature(contestResultFeatures.getNumRightFeature());
//                    userWriter.addFeature(contestResultFeatures.getNumWrongFeature());
//                    userWriter.addFeature(contestResultFeatures.getNumSkippedFeature());
//
//                    Feature<User> scoresFeature = contestResultFeatures.getScoresFeature();
//                    if (!contest.isAllowRestart())
//                        contestScoresFeatures.add(scoresFeature);
//
//                    userWriter.addFeature(scoresFeature);
//
//                    if (appendResults == 1)
//                        contestResultFeatures.appendProblemsFeatures(userWriter);
//
//                    userWriter.addFeature(contestResultFeatures.getProblemsOrderFeature());
//                    userWriter.addFeature(contestResultFeatures.getUserHistoryFeature());
//                }
//            }
//
//            if (appendResults > 0)
//                userWriter.addFeature(new FunctionFeature<User>(contestScoresFeatures) {
//                    @Override
//                    protected String evalFunction(String[] params) {
//                        int sum = 0;
//                        for (String param : params)
//                            sum += Integer.parseInt(param);
//                        return "" + sum;
//                    }
//
//                    @Override
//                    public String name() {
//                        return "sum_scores";
//                    }
//                });
//
//            //write all users
//            while (usersCursor.hasNext()) {
//                User user = new User();
//                user.update(new MongoDeserializer(usersCursor.next()));
//                userWriter.writeObject(user);
//            }
//        } catch (Exception e) {
//            Logger.error("Failed to write user info", e);
//        }
//    }
//
//
//    public static Result getUsersMatrix(final String eventId) {
//        if (User.current().getType() != UserType.EVENT_ADMIN)
//            return forbidden();
//
//        final Event currentEvent = Event.current();
//
//        final String fileName = eventId + "-users-matrix";
//
//        F.Promise<byte[]> promiseOfInt = Akka.future(
//                new Callable<byte[]>() {
//                    public byte[] call() throws IOException {
//
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        ZipOutputStream zos = new ZipOutputStream(baos);
//                        zos.putNextEntry(new ZipEntry(fileName + ".mat"));
//
//                        writeUsersMatrix(fileName, currentEvent, zos);
//
//                        zos.close();
//                        return baos.toByteArray();
//                    }
//                }
//        );
//        return async(
//                promiseOfInt.map(
//                        new F.Function<byte[], Result>() {
//                            public Result apply(byte[] file) {
//                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
//                                return ok(file).as("application/zip");
//                            }
//                        }
//                )
//        );
//    }
//
//    private static void writeUsersMatrix(String fileName, Event event, ZipOutputStream zos) throws IOException {
//        DBObject allUsersQuery = new BasicDBObject(User.FIELD_EVENT, event.getId());
//
//        List<User> allUsers = new ArrayList<>();
//
//        try (DBCursor allUsersCursor = MongoConnection.getUsersCollection().find(allUsersQuery)) {
//            while (allUsersCursor.hasNext())
//                allUsers.add(User.deserialize(new MongoDeserializer(allUsersCursor.next())));
//        }
//
//        Map<User, String> user2answersList = new HashMap<>();
//
//        int usersCount = 0;
//        for (User user : allUsers) {
//            Logger.info("reading answers for user #" + (usersCount++) + ": " + user.getLogin());
//
//            StringBuilder userAnswers = new StringBuilder();
//
//            for (Contest contest : event.getContests()) {
//                if (contest.isAllowRestart())
//                    continue;
//
//                List<String> possibleProblems = contest.getAllPossibleProblems();
//                List<Submission> submissions = user.getSubmissionsForContest(contest);
//
//                for (String possibleProblem : possibleProblems) {
//                    String ans = "-";
//                    //try to find answer in submissions
//                    for (Submission submission : submissions)
//                        if (submission != null && submission.getProblemId().equals('/' + possibleProblem)) {//TODO code duplication
//                            ans = submissionToAnswer(submission);
//                            break;
//                        }
//
//                    userAnswers.append(ans);
//                }
//            }
//
//            user2answersList.put(user, userAnswers.toString());
//        }
//
//        PrintStream out = new PrintStream(zos);
//
//        out.println(allUsers.size());
//
//        for (User userI : allUsers) {
//            String ans1 = user2answersList.get(userI);
//            for (User userJ : allUsers) {
//                String ans2 = user2answersList.get(userJ);
//
//                int cnt = 0;
//                for (int i = 0; i < ans1.length(); i++)
//                    if (ans1.charAt(i) != ans2.charAt(i))
//                        cnt++;
//
//                out.print(cnt);
//                out.print(' ');
//            }
//
//            out.println();
//        }
//
//        out.flush();
//        zos.closeEntry();
//
//        zos.putNextEntry(new ZipEntry(fileName + ".mat.rlabel"));
//        out = new PrintStream(zos);
//        for (User user : allUsers)
//            out.println(user.getId());
//        out.flush();
//        zos.closeEntry();
//
//        zos.putNextEntry(new ZipEntry(fileName + ".mat.clabel"));
//        out = new PrintStream(zos);
//        for (User user : allUsers)
//            out.println(user.getLogin());
//        out.flush();
//        zos.closeEntry();
//    }
//
//}
