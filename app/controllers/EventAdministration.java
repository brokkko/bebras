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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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

    public static Result getUsers(final String eventId, final boolean appendResults) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        final Event currentEvent = Event.current();

        final String fileName = eventId + "-users" + (appendResults ? "-ans" : "");

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
                    //TODO generalize, other answers are different
                    Integer ansInt = (Integer) submission.getAnswer().get("a");
                    int c = ansInt < 0 ? '-' : ansInt + 'A';
                    return Character.toString((char) c);
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
    private static void writeUsersInfo(Event event, OutputStream zos, boolean appendResults) throws IOException {
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

                if (appendResults) {
                    ContestResultFeatures contestResultFeatures = new ContestResultFeatures(contest);
                    userWriter.addFeature(contestResultFeatures.getNumRightFeature());
                    userWriter.addFeature(contestResultFeatures.getNumWrongFeature());
                    userWriter.addFeature(contestResultFeatures.getNumSkippedFeature());

                    Feature<User> scoresFeature = contestResultFeatures.getScoresFeature();
                    if (!contest.isAllowRestart())
                        contestScoresFeatures.add(scoresFeature);

                    userWriter.addFeature(scoresFeature);
                }
            }

            if (appendResults)
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
}