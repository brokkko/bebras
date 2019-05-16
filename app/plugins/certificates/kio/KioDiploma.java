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
        String diploma = (String) user.getInfo().get("diploma");

        if ("0".equals(diploma) || "".equals(diploma))
            diploma = null;

        return diploma;
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
                String.format("Diploma_%s_%s.jpg", diploma2file(), getLevel())
        ).getAbsolutePath();
    }

    private String getLevel() {
        String level = getResult("level");
        if (level == null)
            level = getResult("kiolevel");
        return level;
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

        String surnameName = KioCertificate.surnameName(user);
        canvas.setFontAndSize(KioCertificate.getDefaultFontR(surnameName), 24);
        float y0 = 108;
        canvas.showTextAligned(Element.ALIGN_CENTER, surnameName, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);

        if (isGroup()) {
            y0 = 103;
            canvas.setFontAndSize(KioCertificate.getDefaultFontR(""), 14);
            canvas.showTextAligned(Element.ALIGN_CENTER, "(в команде)", Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
        }

        KioCertificate.drawUserFrom(canvas, user, y0 - 6);

        canvas.setFontAndSize(KioCertificate.getDefaultFontR(""), 17);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Санкт-Петербург " + factory.getYear(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(6), 0);

        canvas.endText();
        canvas.restoreState();
    }
}
