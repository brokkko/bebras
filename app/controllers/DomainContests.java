package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import models.Contest;
import models.Event;
import models.User;
import models.forms.RawForm;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.certificates.bebras.BebrasGramotaCertificate;
import views.html.contests_list_domain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

@LoadEvent
@Authenticated(autoRegister = true)
@DcesController
public class DomainContests extends Controller {

    public static Result contests(String eventId) {
        return ok(contests_list_domain.render());
    }

    public static Result contestAutoregister(String event, String contest) {
        return redirect(controllers.routes.Contests.contest(event, contest, "normal"));
    }

    public static Result showBebrasSchoolCertificate(String event) {
        RawForm form = new RawForm();
        form.bindFromRequest();
        String name = form.get("name");
        String surname = form.get("surname");

        final Document doc = new Document(
                new Rectangle(
                        Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(99)
                ),
                0, 0, 0, 0
        );

        try (AutoCloseable ignored = new AutoCloseable() {
            @Override
            public void close() throws Exception {
                doc.close();
            }
        }) {
            File temporaryCertificate = File.createTempFile("bebras-school-certificate-", ".pdf");

            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(temporaryCertificate)); //TODO do we need to close the writer?

            String bg = Event.current().getEventDataFolder().getAbsolutePath() + "/bg-participants-one.png";
            Image bgImage = Image.getInstance(bg);
            bgImage.setAbsolutePosition(0, 0);
            bgImage.scaleAbsolute(Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(99));

            doc.open();

            doc.newPage();
            doc.add(bgImage);

            draw(writer, name, surname);

            doc.close();

            Controller.response().setHeader("Content-Disposition", "attachment; filename=Bebras Certificate.pdf");
            return Results.ok(temporaryCertificate).as("application/pdf");
        } catch (Exception e) {
            Logger.error("Error while creating certificate", e);
            return ok("Не удалось создать сертификат");
        }
    }

    private static void draw(PdfWriter writer, String name, String surname) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(BebrasGramotaCertificate.ARIAL_FONT_R, 14);
        canvas.showTextAligned(
                Element.ALIGN_RIGHT,
                "Урока Бобра «Знакомство с теорией графов»",
                Utilities.millimetersToPoints(198),
                Utilities.millimetersToPoints(68), 0);
        canvas.setFontAndSize(BebrasGramotaCertificate.ARIAL_FONT_R, 20);
        canvas.showTextAligned(
                Element.ALIGN_CENTER,
                name + " " + surname,
                Utilities.millimetersToPoints(150),
                Utilities.millimetersToPoints(56), 0);

        canvas.endText();
        canvas.restoreState();
    }
}
