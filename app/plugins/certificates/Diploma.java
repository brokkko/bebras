package plugins.certificates;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import models.User;
import models.results.Info;
import play.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public abstract class Diploma<Factory extends DiplomaFactory> {

    protected User user;
    protected Factory factory;
    private Info results = null;

    @Deprecated
    protected Diploma(User user) {
        this.user = user;
    }

    protected Diploma(User user, Factory factory) {
        this.user = user;
        this.factory = factory;

        String contestId = factory.getContestId();
        if (contestId != null)
            results = user.getContestResults(user.getEvent().getContestById(contestId));
    }

    protected Info getResults() {
        if (results == null)
            results = user.getEventResults();
        return results;
    }

    protected String getResult(String field) {
        return (String) getResults().get(field);
    }

    public abstract int getWidthsInMM();

    public abstract int getHeightInMM();

    public abstract String bgPath();

    public abstract boolean isHonored();

    public abstract void draw(PdfWriter writer);

    public File createPdf() {
        final Document doc = new Document(
                new Rectangle(
                        Utilities.millimetersToPoints(getWidthsInMM()), Utilities.millimetersToPoints(getHeightInMM())
                ),
                0, 0, 0, 0
        );

        //TODO report "never used"
        try (AutoCloseable ignored = new AutoCloseable() {
            @Override
            public void close() throws Exception {
                doc.close();
            }
        }) {
            File outputPath = File.createTempFile("pdf-certificate-", ".pdf");

            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));

            Image bgImage = null;

            String bg = bgPath();
            if (bg != null) {
                if (bg.contains("://"))
                    bgImage = Image.getInstance(new URL(bg));
                else
                    bgImage = Image.getInstance(bg);
                bgImage.setAbsolutePosition(0, 0);
                bgImage.scaleAbsolute(Utilities.millimetersToPoints(getWidthsInMM()), Utilities.millimetersToPoints(getHeightInMM()));
            }

            doc.open();

            doc.newPage();
            if (bgImage != null)
                doc.add(bgImage);

            draw(writer);

            doc.close();

            return outputPath;

        } catch (Exception e) {
            Logger.error("Error while creating certificate", e);
        }

        return null;
    }
}