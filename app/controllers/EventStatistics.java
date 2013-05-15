package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.actions.Authenticated;
import controllers.actions.LoadEvent;
import models.Event;
import models.User;
import models.forms.InputField;
import models.serialization.Deserializer;
import models.serialization.MongoDeserializer;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
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
public class EventStatistics extends Controller {

    public static Result getUsers(final String eventId) {
        F.Promise<byte[]> promiseOfInt = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws IOException {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);
                        zos.putNextEntry(new ZipEntry(eventId + ".users.csv"));

                        writeUsersInfo(Event.current(), zos);

                        zos.close();
                        return baos.toByteArray();
                    }
                }
        );
        return async(
                promiseOfInt.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    private static void writeUsersInfo(Event event, OutputStream zos) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject query = new BasicDBObject(User.FIELD_EVENT, event.getId());

        DBCursor usersCursor = usersCollection.find(query);

        try {
            while (usersCursor.hasNext()) {
                writeUserInfo(new MongoDeserializer(usersCursor.next()), event.getEditUserForm().getFields(), zos);
            }
        } finally {
            usersCursor.close();
        }
    }

    private static void writeUserInfo(Deserializer userDeserializer, Collection<? extends InputField> fields, OutputStream zos) {
        User user = new User();
        user.update(userDeserializer);

        for (InputField field : fields) {

        }
    }

}
