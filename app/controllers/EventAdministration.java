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
import java.util.Date;
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

    public static Result getUsers(final String eventId) {
        if (User.current().getType() != UserType.EVENT_ADMIN)
            return forbidden();

        final Event currentEvent = Event.current();

        F.Promise<byte[]> promiseOfInt = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws IOException {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);
                        zos.putNextEntry(new ZipEntry(eventId + "-users.csv"));

                        writeUsersInfo(currentEvent, zos);

                        zos.close();
                        return baos.toByteArray();
                    }
                }
        );
        return async(
                promiseOfInt.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                response().setHeader("Content-Disposition", "attachment; filename=" + currentEvent.getId() + "-users.zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
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
    private static void writeUsersInfo(Event event, OutputStream zos) throws IOException {
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

            for (final Contest contest : event.getContests())
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