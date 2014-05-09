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

import java.io.File;

public class CertificatePlugin extends Plugin {

    private String viewRight;
    private DiplomaFactory diplomaFactory; //TODO replace with serialization type
    private String viewTitle;

    @Override
    public void initPage() {
        //do nothing
        //TODO test if is honoured and add to menu
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

        Diploma diploma = diplomaFactory.getCertificate(user);

        if (diploma == null)
            return Results.notFound("diploma type unknown");

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