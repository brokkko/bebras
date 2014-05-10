package plugins.certificates;

import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import views.Menu;

import java.io.File;

public class DiplomaPlugin extends Plugin {

    public static final String PLUGIN_NAME = "Diploma";
    private String viewRight;
    private DiplomaFactory diplomaFactory;
    private String viewTitle;

    @Override
    public void initPage() {
        if (!User.currentRole().hasRight(viewRight))
            return;

        User user = User.current();

        if (user != null) { //TODO user may be null (as far as I understand) only for Domain menu entry. Think what to do with showing diploma
            Diploma diploma = diplomaFactory.getDiploma(user);
            if (!diploma.isHonored())
                return;

            Menu.addMenuItem(viewTitle, getCall(), viewRight);
        }
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public Result doGet(String action, String params) {
        switch (action) {
            case "go":
                return showCertificate();
        }
        return Results.notFound();
    }

    @Override
    public Result doPost(String action, String params) {
        return Results.notFound();
    }

    private Result showCertificate() {
        User user = User.current();
        if (!user.hasRight(viewRight))
            return Results.forbidden("Oops...");

        Diploma diploma = diplomaFactory.getDiploma(user);

        if (diploma == null)
            return Results.notFound("diploma type unknown");

        if (!diploma.isHonored())
            return Results.forbidden("You are not honoured with this certificate");

        File temporaryCertificate = diploma.createPdf();

        //TODO may be class simple name is not the best thing to name a file
        Controller.response().setHeader("Content-Disposition", "attachment; filename=" + diploma.getClass().getSimpleName() + "-" + user.getLogin() + ".pdf");
        return Results.ok(temporaryCertificate).as("application/pdf");
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        viewRight = deserializer.readString("view right");
        diplomaFactory = SerializationTypesRegistry.CERTIFICATE_FACTORY.read(deserializer, "diploma");
        viewTitle = deserializer.readString("view title");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("view right", viewRight);
        SerializationTypesRegistry.CERTIFICATE_FACTORY.write(serializer, "diploma", diplomaFactory);
        serializer.write("view title", viewTitle);
    }
}