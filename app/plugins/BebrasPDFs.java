package plugins;

import models.Event;
import models.User;
import models.Utils;
import models.applications.Application;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.cache.Cache;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.Menu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 17.10.13
 * Time: 16:06
 */
public class BebrasPDFs extends Plugin {

    private Date time;
    private String applicantRole = "school org";

    @Override
    public void initPage() {
        if (!User.currentRole().hasRight(applicantRole))
            return;

        if (new Date().before(time) && !User.current().hasEventAdminRight())
            return;

        if (getParticipants() > 0 || User.current().hasEventAdminRight())
            Menu.addMenuItem("PDF условия задач", getCall(), applicantRole);
    }

    private int getParticipants() {
        User user = User.current();

        if (user == null)
            return 0;

        //noinspection unchecked
        List<Application> apps = (List<Application>) user.getInfo().get("apps"); //get apps from config

        if (apps == null)
            return 0;

        int cnt = 0;
        for (Application app : apps)
            if ("pdf".equals(app.getType()))//get apps from
                cnt += app.getSize();

        return cnt;
    }

    @Override
    public void initEvent(Event event) {
    }

    @Override
    public Result doGet(String action, String params) {
        if (!User.currentRole().hasRight(applicantRole))
            return Results.forbidden();

        if (new Date().before(time) && !User.current().hasEventAdminRight())
            return Results.forbidden();

        if ("go".equals(action))
            return Results.ok(views.html.bebraspdf.pdf_list.render(this));

        if (action.startsWith("get-pdf-")) {
            return getPdf(action.substring("get-pdf-".length()));
        }

        return Results.notFound();
    }

    private Result getPdf(final String fname) {
        final File pdf = new File(Event.current().getEventDataFolder(), fname + ".pdf");
        final int participants = getParticipants();

        F.Promise<byte[]> promiseOfVoid = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                            byte[] contents = Cache.getOrElse("bebras-pdf-" + fname, new Callable<byte[]>() {
                                @Override
                                public byte[] call() throws Exception {
                                    return Utils.readFileAsBytes(pdf);
                                }
                            }, 60 * 60);

                            for (int i = 1; i <= participants; i++) {
                                zos.putNextEntry(new ZipEntry(i + " " + fname + ".pdf"));
                                zos.write(contents);
                            }

                        } catch (Exception e) {
                            return null;
                        }

                        return baos.toByteArray();
                    }
                }
        );

        return Results.async(
                promiseOfVoid.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                if (file == null)
                                    return Results.ok("Не удалось загрузить файл, попробуйте еще раз");
                                Controller.response().setHeader("Content-Disposition", "attachment; filename=" + fname + ".zip");
                                return Results.ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    public Call getPdfFileCall(String fileName) {
        return getCall("get-pdf-" + fileName);
    }

    @Override
    public Result doPost(String action, String params) {
        return null;
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        String sTime = deserializer.readString("time", null);
        if (sTime == null)
            time = new Date();
        else
            time = Utils.parseSimpleTime(sTime);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);    //To change body of overridden methods use File | Settings | File Templates.
        serializer.write("time", Utils.formatDateTimeForInput(time));
    }

    // upload files

}
