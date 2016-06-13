package plugins.certificates.kio;

import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.ServerConfiguration;
import models.User;
import models.results.Info;
import plugins.certificates.Diploma;

public class KioTeacherCertificate extends Diploma<KioTeacherCertificateFactory> {

    protected KioTeacherCertificate(User user, KioTeacherCertificateFactory factory) {
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
        return ServerConfiguration.getInstance().getPluginFile(KioCertificate.PLUGIN_NAME, "Teacher Certificate.png").getAbsolutePath();
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

        int yPos = 153;

        canvas.showTextAligned(Element.ALIGN_CENTER, surnameNamePatronymic(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(yPos), 0);

        KioCertificate.drawUserFrom(canvas, user, yPos - 8);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 17);
        canvas.showTextAligned(Element.ALIGN_LEFT, "(КИО " + factory.getYear() % 100 + ")", Utilities.millimetersToPoints(141), Utilities.millimetersToPoints(106.5f), 0);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 17);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Санкт-Петербург " + factory.getYear(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(6), 0);

        canvas.endText();
        canvas.restoreState();
    }

    //TODO may need to make it static
    private String surnameNamePatronymic() {
        Info info = user.getInfo();
        return (info.get("surname") + " " + info.get("name") + " " + info.get("patronymic")).toUpperCase();
    }
}
