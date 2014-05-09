package plugins.certificates.kio;

import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.ServerConfiguration;
import models.User;
import plugins.certificates.Diploma;

public class KioProblemDiploma extends Diploma<KioProblemDiplomaFactory> {

    protected KioProblemDiploma(User user, KioProblemDiplomaFactory factory) {
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
        String fileName = "Problem_Diploma_" + getResult("level") + ".png";
        return ServerConfiguration.getInstance().getPluginFile(KioCertificate.PLUGIN_NAME, fileName).getAbsolutePath();
    }

    @Override
    public boolean isHonored() {
        return user.getInfo().get(factory.getDiplomaField()) != null;
    }

    private boolean isGroup() {
        String diploma = (String) user.getInfo().get(factory.getDiplomaField());
        if (diploma == null)
            throw new IllegalStateException("Problem diploma illegal state");
        return diploma.endsWith("g");
    }

    private int getLevel() {
        String level = getResult("level");
        if (level == null)
            throw new IllegalStateException("null level in Kio Problem Diploma");
        switch (level) {
            case "1":
                return 1;
            case "2":
                return 2;
            case "0":
                return 0;
        }

        throw new IllegalStateException("can not parse level in Kio Problem Diploma");
    }

    @Override
    public void draw(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 24);
        canvas.showTextAligned(Element.ALIGN_CENTER, KioCertificate.surnameName(user), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(120), 0);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 14);
        float y0 = 115;
        float lineSkip = 5;

        if (isGroup()) {
            canvas.showTextAligned(Element.ALIGN_CENTER, "(в команде)", Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
            y0 -= lineSkip;
        }

        String problemDescription = String.format("Задача «%s»: %s место", factory.getProblemName(getLevel()), getResult(factory.getProblemRankField()));
        canvas.showTextAligned(Element.ALIGN_CENTER, problemDescription, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);

        canvas.setFontAndSize(KioCertificate.DEFAULT_FONT_R, 17);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Санкт-Петербург " + factory.getYear(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(6), 0);

        canvas.endText();
        canvas.restoreState();
    }
}
