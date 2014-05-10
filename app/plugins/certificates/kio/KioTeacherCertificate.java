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
        return ServerConfiguration.getInstance().getPluginFile(KioCertificate.PLUGIN_NAME, "Gramota.png").getAbsolutePath();
    }

    @Override
    public boolean isHonored() {
        Object teacherGramota = user.getInfo().get("teacher_gramota");
        return teacherGramota != null && !"0".equals(teacherGramota) && !"".equals(teacherGramota);
    }

    @Override
    public void draw(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 24);
        canvas.showTextAligned(Element.ALIGN_CENTER, surnameNamePatronymic(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(110), 0);

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
