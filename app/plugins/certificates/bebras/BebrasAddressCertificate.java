package plugins.certificates.bebras;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.User;
import play.Logger;
import plugins.BebrasPlacesEvaluator;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

public class BebrasAddressCertificate extends Diploma<DiplomaFactory> {

    private static final float ADDRESS_BOX_WIDTH = 112;
    private static final float ADDRESS_BOX_HEIGHT = 39;
    private static final float ADDRESS_PADDING = 10;

    private static final Font DEFAULT_FONT_R = new Font(BebrasCertificateLine.DEFAULT_FONT_R, 12f, Font.NORMAL);
    private static final Font DEFAULT_FONT_B = new Font(BebrasCertificateLine.DEFAULT_FONT_B, 12f, Font.NORMAL);
    private int year;

    public BebrasAddressCertificate(User user, int year) {
        super(user);
        this.year = year;
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
        return null;
    }

    @Override
    public boolean isHonored() {
        return true;
    }

    @Override
    public void draw(PdfWriter writer) {
        draw(writer, -1);
    }

    public void draw(PdfWriter writer, int position) {
        float ls = Utilities.pointsToMillimeters(12f * 1.2f);
        float x0 = 1;
        float y0 = ADDRESS_BOX_HEIGHT - ls;
        float WHOM_WIDTH = Utilities.pointsToMillimeters(new Chunk("Кому: ", DEFAULT_FONT_B).getWidthPoint());
        float WHERE_WIDTH = Utilities.pointsToMillimeters(new Chunk("Куда: ", DEFAULT_FONT_B).getWidthPoint());

        printAddrText(writer, BebrasCertificate.getUserCode(user, true, year), false, x0, y0, position);

        y0 -= ls;

        printAddrText(writer, "Кому: ", true, x0, y0, position);
        printAddrText(writer,
                capitalize((String) user.getInfo().get("surname")) + " " + capitalize((String) user.getInfo().get("name")) + " " + capitalize((String) user.getInfo().get("patronymic")),
                false, x0 + WHOM_WIDTH, y0, position
        );

        Object sendToSelfObj = user.getInfo().get("send_to_self");
        boolean sendToSelf = sendToSelfObj == null ? false : (Boolean) sendToSelfObj;

        if (!sendToSelf) {
            String[] schoolLines = BebrasPlacesEvaluator.splitProbablyLongLine((String) user.getInfo().get("school_name"));
            for (String schoolLine : schoolLines) {
                y0 -= ls;
                printAddrText(writer, shortenText(schoolLine), false, x0, y0, position);
            }
        }

        y0 -= ls;
        printAddrText(writer, "Куда: ", true, x0, y0, position);
        printAddrText(writer, (String) user.getInfo().get("index"), false, x0 + WHERE_WIDTH, y0, position);

        String[] addressLines = BebrasPlacesEvaluator.splitProbablyLongLine((String) user.getInfo().get("address"));

        for (String addressLine : addressLines) {
            y0 -= ls;
            printAddrText(writer, shortenText(addressLine), false, x0, y0, position);
        }
    }

    //position = 0 1 2 3 4 5
    private boolean printAddrText(PdfWriter writer, String text, boolean bold, float x0, float y0, int position) {
        float tx = ADDRESS_PADDING;
        float ty = 297 - ADDRESS_PADDING - (position + 1) * ADDRESS_BOX_HEIGHT;

        float angle;
        if (position > 6) {
            tx = ADDRESS_PADDING + ADDRESS_BOX_WIDTH;
            ty = 297 - ADDRESS_PADDING;
            switch (position) {
                case 7:
                    //do nothing
                    break;
                case 8:
                    tx += ADDRESS_BOX_HEIGHT;
                    break;
                case 9:
                    ty -= ADDRESS_BOX_WIDTH;
                    break;
                case 10:
                    tx += ADDRESS_BOX_HEIGHT;
                    ty -= ADDRESS_BOX_WIDTH;
                    break;
            }
            angle = -90;
            tx += y0;
            ty -= x0;
        } else {
            tx += x0;
            ty += y0;
            angle = 0;
        }

        Font font = bold ? DEFAULT_FONT_B : DEFAULT_FONT_R;
        float textWidth = font.getBaseFont().getWidthPoint(text, font.getSize());
        float textWidthMM = Utilities.pointsToMillimeters(textWidth);
        boolean wasError = false;
        if (textWidthMM + x0 > ADDRESS_BOX_WIDTH) {
            Logger.warn("Too wide text (" + (textWidthMM + x0) + " mm.): " + user.getLogin() + " |" + text + "|");
            wasError = true;
        }

        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setFontAndSize(font.getBaseFont(), font.getSize());
        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);
        canvas.showTextAligned(Element.ALIGN_LEFT, text, Utilities.millimetersToPoints(tx), Utilities.millimetersToPoints(ty), angle);

        canvas.endText();
        canvas.restoreState();

        return wasError;
    }

    private String shortenText(String text) {
        return text;
    }

    private static String capitalize(String name) {
        if (name.length() < 1)
            return name;
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
