package plugins;

import au.com.bytecode.opencsv.CSVReader;
import models.Event;
import models.User;
import org.bson.types.ObjectId;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.Menu;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.08.13
 * Time: 15:55
 */
public class FieldsUploader extends Plugin {

    @Override
    public void initPage() {
        Menu.addMenuItem("Загрузить данные", getCall(), "event admin");
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public Result doGet(String action) {
        if (User.currentRole().hasRight("event admin"))
            return Results.ok(views.html.upload_fields.render(Controller.flash("ok"), Controller.flash("error"), getCall("go", false)));
        return Results.forbidden();
    }

    @Override
    public Result doPost(String action) {
        if (!User.currentRole().hasRight("event admin"))
            return Results.forbidden();

        Http.MultipartFormData body = Http.Context.current().request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart newFields = body.getFile("new-fields");
        if (newFields == null) {
            Controller.flash("error", "Не выбран файл дял загрузки");
            return Results.redirect(getCall());
        }

        File fieldsFile = newFields.getFile();
        try {
            loadFile(fieldsFile);
            Controller.flash("ok", "Данные успешно загружены");
            return Controller.redirect(getCall());
        } catch (Exception e) {
            Controller.flash("error", "При загрузке данных произошла ошибка: " + e.getMessage());
            Logger.error("error loading file " + fieldsFile, e);
            return Controller.redirect(getCall());
        }
    }

    private void loadFile(File file) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "windows-1251"), ';', '"');
        String[] title = reader.readNext();
        if (title == null)
            throw new IOException("No title in csv file");
        //find _id index

        String[] line;
        while ((line = reader.readNext()) != null) {
            ObjectId id = new ObjectId(line[0]);
            User user = User.getInstance("_id", id);
            if (user == null)
                continue;
            for (int i = 0; i < Math.min(title.length, line.length); i++)
                user.getInfo().put(title[i], line[i]);

            user.invalidateAllResults();
        }
    }
}
