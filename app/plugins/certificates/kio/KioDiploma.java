package plugins.certificates.kio;

import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.ServerConfiguration;
import models.User;
import plugins.certificates.Diploma;

public class KioDiploma extends Diploma<KioDiplomaFactory> {

    public KioDiploma(User user, KioDiplomaFactory factory) {
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

    private String getDiploma() {
        return (String) user.getInfo().get("diploma");
    }

    private boolean isGroup() {
        String diploma = getDiploma();
        return diploma != null && diploma.endsWith("g");
    }

    private String diploma2file() {
        String diploma = getDiploma();
        if (diploma == null)
            throw new IllegalStateException("Can not get kio diploma file name for a user not honoured with a diploma");

        if (diploma.endsWith("g"))
            diploma = diploma.substring(0, diploma.length() - 1);

        switch (diploma) {
            case "1":
                return "I";
            case "2":
                return "II";
            case "3":
                return "III";
        }

        throw new IllegalStateException("Kio Diploma with unknown type");
    }

    @Override
    public String bgPath() {
        return ServerConfiguration.getInstance().getPluginFile(KioCertificate.PLUGIN_NAME,
                String.format("Diploma_%s_%s.jpg", diploma2file(), getResult("level"))
        ).getAbsolutePath();
    }

    @Override
    public boolean isHonored() {
        return getDiploma() != null;
    }

    @Override
    public void draw(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 24);
        canvas.showTextAligned(Element.ALIGN_CENTER, KioCertificate.surnameName(user), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(110), 0);

        if (isGroup()) {
            canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 14);
            canvas.showTextAligned(Element.ALIGN_CENTER, "(в команде)", Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(105), 0);
        }

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 17);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Санкт-Петербург " + factory.getYear(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(6), 0);

        canvas.endText();
        canvas.restoreState();
    }
}
