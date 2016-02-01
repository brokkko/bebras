package plugins.certificates.bebras;

import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.ServerConfiguration;
import models.User;
import models.results.Info;
import plugins.BebrasPlacesEvaluator;
import plugins.certificates.Diploma;
import plugins.certificates.kio.KioCertificate;

public class BabiorCertificate extends Diploma<BabiorCertificateFactory> {

    private static final String PLUGIN_NAME = "BabiorDiplomas";

    public BabiorCertificate(User user, BabiorCertificateFactory factory) {
        super(user, factory);
    }

    @Override
    public int getWidthsInMM() {
        return 210;
    }

    @Override
    public int getHeightInMM() {
        return 297;
    }

    @Override
    public String bgPath() {
        return ServerConfiguration.getInstance().getPluginFile(BabiorCertificate.PLUGIN_NAME,
                String.format("BabiorCertificate%d.jpg", factory.getYear())
        ).getAbsolutePath();
    }

    @Override
    public boolean isHonored() {
        return true;
    }

    @Override
    public void draw(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 24);
        float y0 = 143;
        canvas.showTextAligned(Element.ALIGN_CENTER, surnameName(user), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);

        drawUserFrom(canvas, user, y0 - 6);

        canvas.endText();
        canvas.restoreState();
    }

    public String surnameName(User user) {
        return String.format("%s %s", user.getInfo().get("surname"), user.getInfo().get("name")).toUpperCase();
    }

    public void drawUserFrom(PdfContentByte canvas, User user, float y0) {
        User regBy = user.getRegisteredByUser();

        String schoolLine = null;
        if (regBy != null) {
            Info info = regBy.getInfo();
            schoolLine = (String) info.get("school_name");
        }

        if (schoolLine == null)
            schoolLine = "";

        schoolLine = schoolLine.replaceAll("  ", " ").replaceAll("[\n\r]+", " ");

        String[] schoolLines = BebrasPlacesEvaluator.splitProbablyLongLine(schoolLine);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 12);
        for (String line : schoolLines) {
            canvas.showTextAligned(Element.ALIGN_CENTER, line, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
            y0 -= 4;
        }
    }
}
