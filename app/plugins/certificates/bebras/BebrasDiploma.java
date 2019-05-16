package plugins.certificates.bebras;

import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.User;
import models.results.Info;
import plugins.BebrasPlacesEvaluator;
import plugins.certificates.Diploma;
import plugins.certificates.kio.KioCertificate;

public class BebrasDiploma extends Diploma<BebrasDiplomaFactory> {

    private int level;

    protected BebrasDiploma(User user, BebrasDiplomaFactory factory) {
        super(user, factory);
        try {
            level = Integer.parseInt((String) user.getInfo().get(factory.getUserDiplomaLevelField()));
        } catch (NumberFormatException e) {
            level = 0;
        }
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
        return user.getEvent().getEventDataFolder().getAbsolutePath() + "/diploma2014-" + level + ".png"; //TODO get current year
    }

    @Override
    public boolean isHonored() {
        return level > 0;
    }

    @Override
    public void draw(PdfWriter writer) {
        User org = user.getRegisteredByUser();
        Info orgInfo = org.getInfo();

        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(BebrasGramotaCertificate.ARIAL_FONT_R, 14);
        float y0 = 156;
        canvas.showTextAligned(Element.ALIGN_CENTER, "НАГРАЖДАЕТСЯ ПОБЕДИТЕЛЬ", Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
        y0 -= 7;
        canvas.showTextAligned(Element.ALIGN_CENTER, "МЕЖДУНАРОДНОГО КОНКУРСА ПО ИНФОРМАТИКЕ «Бобёр-" + factory.getYear() + "»", Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
        y0 -= 9;
        canvas.setFontAndSize(BebrasGramotaCertificate.ARIAL_FONT_B, 14);
        canvas.showTextAligned(Element.ALIGN_CENTER, user.getInfo().get("surname") + " " + user.getInfo().get("name"), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);

        canvas.setFontAndSize(BebrasGramotaCertificate.ARIAL_FONT_R, 11);

        y0 -= 8;
        canvas.showTextAligned(Element.ALIGN_CENTER, "ученик(ца) " + user.getInfo().get("grade") + " класса", Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
        String rawSchoolName = (String) orgInfo.get("school_name");
        for (String schoolName : BebrasPlacesEvaluator.splitProbablyLongLine(rawSchoolName)) {
            y0 -= 5.5f;
            canvas.showTextAligned(Element.ALIGN_CENTER, schoolName, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
        }
        String rawOrgAddress = BebrasPlacesEvaluator.getOrgAddress(orgInfo);
        for (String address : BebrasPlacesEvaluator.splitProbablyLongLine(rawOrgAddress)) {
            y0 -= 5.5f;
            canvas.showTextAligned(Element.ALIGN_CENTER, address, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
        }

        canvas.setFontAndSize(KioCertificate.getDefaultFontR(""), 17);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Санкт-Петербург " + factory.getYear(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(6), 0);

        canvas.endText();
        canvas.restoreState();

        BebrasGramotaCertificate.formatDiplomGramotaFooter(writer, true);
    }
}
