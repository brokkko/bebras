package plugins.certificates;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import models.Event;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import plugins.certificates.kio.KioCertificate;
import views.Menu;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
            case "all":
                return showAllCertificates();
        }
        return Results.notFound();
    }

    @Override
    public Result doPost(String action, String params) {
        return Results.notFound();
    }

    private Result showCertificate() {
        if (!User.currentRole().hasRight(viewRight))
            return Results.forbidden("Oops...");

        User user = User.current();
        Diploma diploma = diplomaFactory.getDiploma(user);

        if (diploma == null)
            return Results.notFound("diploma type unknown");

        if (!diploma.isHonored())
            return Results.forbidden("You are not honoured with this certificate");

        File temporaryCertificate = diploma.createPdf();

        //TODO may be class simple name is not the best thing to name a file
        return getPdfResult(diploma.getClass().getSimpleName() + "-" + user.getLogin(), temporaryCertificate);
    }

    //TODO some code duplication with Diploma.createPDF()
    private Result showAllCertificates() {
        DBObject query = new BasicDBObject(User.FIELD_EVENT, Event.current().getId());
        User user = User.current();
        if (!user.hasEventAdminRight())
            query.put(User.FIELD_REGISTERED_BY, user.getId());

        //TODO now page size is fixed, implement positions as in bebras certificate
        final Document doc = new Document(
                new Rectangle(
                        Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(297)
                ),
                0, 0, 0, 0
        );

        try (
                User.UsersEnumeration usersEnumeration = User.listUsers(query);
                AutoCloseable ignored = new AutoCloseable() {
                    @Override
                    public void close() throws Exception {
                        doc.close();
                    }
                }
        ) {
            File outputPath = File.createTempFile("pdf-all-certificates-", ".pdf");

            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));

            Map<String, Image> bgPath2Image = new HashMap<>();

            doc.open();

            boolean noPages = true;
            while (usersEnumeration.hasMoreElements()) {
                User subUser = usersEnumeration.nextElement();

                if (subUser.isPartialRegistration())
                    continue;

                if (!subUser.hasRight(viewRight))
                    continue;

                Diploma diploma = diplomaFactory.getDiploma(subUser);

                if (!diploma.isHonored())
                    continue;

                Image bgImage = null;

                String bg = diploma.bgPath();
                if (bg != null) {
                    bgImage = bgPath2Image.get(bg);
                    if (bgImage == null) {
                        if (bg.contains("://"))
                            bgImage = Image.getInstance(new URL(bg));
                        else
                            bgImage = Image.getInstance(bg);
                        bgImage.setAbsolutePosition(0, 0);
                        bgImage.scaleAbsolute(Utilities.millimetersToPoints(diploma.getWidthsInMM()), Utilities.millimetersToPoints(diploma.getHeightInMM()));

                        bgPath2Image.put(bg, bgImage);
                    }
                }

                doc.newPage();
                noPages = false;
                if (bgImage != null)
                    doc.add(bgImage);
                diploma.draw(writer);
            }

            if (noPages)
                writeDocumentIsEmpty(doc, writer);

            doc.close();

            return getPdfResult("all-" + getRef(), outputPath);
        } catch (Exception e) {
            Logger.error("Failed to show all certificates", e);
            return Results.internalServerError();
        }
    }

    private void writeDocumentIsEmpty(Document doc, PdfWriter writer) {
        doc.newPage();

        PdfContentByte canvas = writer.getDirectContent();

        canvas.saveState();
        canvas.beginText();

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 42); //TODO take font from some normal place
        for (int y0 = 280; y0 > 0; y0 -= 30)
            canvas.showTextAligned(Element.ALIGN_CENTER, "Этот документ пустой", Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);

        canvas.endText();
        canvas.restoreState();
    }

    private Result getPdfResult(String fileName, File temporaryCertificate) {
        Controller.response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".pdf");
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