package plugins.certificates.bebras;

import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.Event;
import models.User;
import models.applications.Application;
import org.bson.types.ObjectId;
import play.Logger;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

import java.util.List;

public class BebrasCertificate extends Diploma<DiplomaFactory> {

    private static final float TEXT_BOX_LEFT = 94;
    private static final float TEXT_BOX_BOOTOM = 58;
    private static final float TEXT_BOX_WIDTH = 112;
    private static final float TEXT_BOX_HEIGHT = 58;

    private boolean org;
    private List<BebrasCertificateLine> lines;
    private int year = 2013;

    private static final ObjectId NOVOSIBIRSK_ID_13 = User.getUserByLogin("bebras13", "shkola-plus").getId();
    private static final ObjectId NOVOSIBIRSK_ID_14 = User.getUserByLogin("bebras14", "school_plus").getId();
    private static final ObjectId NOVOSIBIRSK_ID_15 = User.getUserByLogin("bebras15", "school_plus").getId();
    private static final ObjectId NOVOSIBIRSK_ID_16 = User.getUserByLogin("bebras16", "school_plus").getId();

    public static boolean isNovosibirsk(ObjectId objectId) {
        return NOVOSIBIRSK_ID_13.equals(objectId) ||
                NOVOSIBIRSK_ID_14.equals(objectId) ||
                NOVOSIBIRSK_ID_15.equals(objectId) ||
                NOVOSIBIRSK_ID_16.equals(objectId);
    }

    public BebrasCertificate(User user, boolean org, List<BebrasCertificateLine> lines, int year) {
        super(user);
        this.org = org;
        this.lines = lines;
        this.year = year;
    }

    @Override
    public int getWidthsInMM() {
        return 210;
    }

    @Override
    public int getHeightInMM() {
        return 99;
    }

    @Override
    public String bgPath() {
        return Event.current().getEventDataFolder().getAbsolutePath() + "/" + (org ? "bg-organizers-one.png" : "bg-participants-one.png");
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
        float x0 = TEXT_BOX_LEFT + TEXT_BOX_WIDTH / 2f;
        float y0 = TEXT_BOX_BOOTOM;

        float lineSkip = lines.size() >= 10 ? 1.3f : 1.5f;

        boolean firstLine = true;
        for (BebrasCertificateLine line : lines) {
            if (firstLine)
                firstLine = false;
            else
                y0 -= Utilities.pointsToMillimeters(line.getSize() * lineSkip);
            printText(writer, line.getBaseFont(), line.getLine(), line.getSize(), x0, y0, position);
        }

        String userCode = getUserCode(user, org, year);

        printText(writer, BebrasCertificateLine.DEFAULT_FONT_R, userCode, 12, 49, 4, position);
    }

    public static String getUserCode(User user, boolean org, int year) {
        String userCode = org ? (
                year == 2013 ? Application.getCodeForUserHex(user) : Application.getCodeForUser(user)
        ) : user.getLogin();

        //write login for novosibirsk teachers
        if (org && isNovosibirsk(user.getRegisteredBy()))
            userCode = user.getLogin() + " " + userCode;
        return userCode;
    }

    //position = 0 1 2 3 4 5
    private boolean printText(PdfWriter writer, BaseFont font, String text, float size, float x0, float y0, int position) {
        boolean upDown = position != 2 && position != 3 && position != -1;
        float angle = upDown ? 180 : 0;

        float tx, ty;

        if (position != -1) {
            tx = 15 + 210 * (position % 2 + (upDown ? 1 : 0)); // 15 210 210 15
            ty = 11.5f + 99 * (2 - position / 2 + (upDown ? 1 : 0)); // 11.5 99 99 99 11.5
        } else {
            tx = 0;
            ty = 0;
        }

        if (upDown)
            ty -= y0;
        else
            ty += y0;

        if (upDown)
            tx -= x0;
        else
            tx += x0;

        float textWidth = font.getWidthPoint(text, size);

        float textWidthMM = Utilities.pointsToMillimeters(textWidth);
        boolean wasError = false;
        if (textWidthMM > TEXT_BOX_WIDTH) {
            Logger.warn("Too wide text (" + textWidthMM + " mm.): " + user.getLogin() + " " + text + "|");
            wasError = true;
        }

        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setFontAndSize(font, size);
        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);
        canvas.showTextAligned(Element.ALIGN_CENTER, text, Utilities.millimetersToPoints(tx), Utilities.millimetersToPoints(ty), angle);

        canvas.endText();
        canvas.restoreState();

        return wasError;
    }
}
