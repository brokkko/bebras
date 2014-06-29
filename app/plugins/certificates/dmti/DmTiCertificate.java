package plugins.certificates.dmti;

import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.ServerConfiguration;
import models.User;
import models.results.Info;
import plugins.certificates.Diploma;

public class DmTiCertificate extends Diploma<DmTiCertificateFactory> {

    public DmTiCertificate(User user, DmTiCertificateFactory factory) {
        super(user, factory);
    }

    public String getCertificateType() {
        return (String) user.getInfo().get("dm_ti_certificate"); //TODO may be extract to configuration
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
        return ServerConfiguration.getInstance().getPluginFile(ThankYouLetter.PLUGIN_NAME, "certificate.jpg").getAbsolutePath();
    }

    @Override
    public boolean isHonored() {
        return getCertificateType() != null && !getCertificateType().equals("");
    }

    @Override
    public void draw(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(ThankYouLetter.DEFAULT_FONT_R, 24);

        Info info = user.getInfo();
        String[] userFrom = ThankYouLetter.getUserFrom(user);
        String schoolLine = userFrom[0];
        String addressLine = userFrom[1];

        String surnameName = String.format("%s %s",
                ThankYouLetter.capitalize(info.get("surname")),
                ThankYouLetter.capitalize(info.get("name"))
        );

        String grade = (String) user.getInfo().get("grade");
        Integer scores = (Integer) getResults().get("scores");

        float lineSkip = 18;

        float y0 = Utilities.millimetersToPoints(150);

        canvas.setFontAndSize(ThankYouLetter.DEFAULT_FONT_R, 14);
        canvas.showTextAligned(Element.ALIGN_CENTER,
                String.format("ученик(ца) %s класса", grade),
                Utilities.millimetersToPoints(105), y0, 0
        );

        canvas.setFontAndSize(ThankYouLetter.DEFAULT_FONT_B, 14);
        canvas.showTextAligned(Element.ALIGN_CENTER, surnameName, Utilities.millimetersToPoints(105), y0 - lineSkip, 0);

        canvas.setFontAndSize(ThankYouLetter.DEFAULT_FONT_R, 14);
        canvas.showTextAligned(Element.ALIGN_CENTER, schoolLine, Utilities.millimetersToPoints(105), y0 - 2 * lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, addressLine, Utilities.millimetersToPoints(105), y0 - 3 * lineSkip, 0);

        String line1 = "участвовал(а) в Первой заочной олимпиаде по дискретной";
        String line2 = "математике и теоретической информатике";
        String line3 = "";

        if (getCertificateType().equals("scores")) {
            line2 += " и получил(а)";
            if (scores == 4)
                line3 = scores + " балл из 12 возможных.";
            else
                line3 = scores + " баллов из 12 возможных.";
        } else
            line2 += ".";

        canvas.showTextAligned(Element.ALIGN_CENTER, line1, Utilities.millimetersToPoints(105), y0 - 5 * lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, line2, Utilities.millimetersToPoints(105), y0 - 6 * lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, line3, Utilities.millimetersToPoints(105), y0 - 7 * lineSkip, 0);
/*
        if ("scores".equals(getCertificateType())) {
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    String.format("ученики котор%s достигли хороших результатов", (surnameName) ? "ого" : "ой"),
                    Utilities.millimetersToPoints(105), y0 - 4 * lineSkip, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, "в заочном дистанционном туре олимпиады.", Utilities.millimetersToPoints(105), y0 - 5 * lineSkip, 0);
        } else {
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    String.format("ученики котор%s приняли активное участие", (surnameName) ? "ого" : "ой"),
                    Utilities.millimetersToPoints(105), y0 - 4 * lineSkip, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, "в заочном дистанционном туре олимпиады.", Utilities.millimetersToPoints(105), y0 - 5 * lineSkip, 0);
        }
*/

        canvas.endText();
        canvas.restoreState();
    }
}
