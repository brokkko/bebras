package plugins.certificates.dmti;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.ServerConfiguration;
import models.User;
import models.results.Info;
import play.Logger;
import plugins.certificates.Diploma;

import java.io.File;
import java.io.IOException;

public class ThankYouLetter extends Diploma<ThankYouLetterFactory> {

    public static final String PLUGIN_NAME = "DmTiDiplomas";
    public static final File R_FONT_FILE = ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "times.ttf");
    public static BaseFont DEFAULT_FONT_R;
    public static final File B_FONT_FILE = ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "timesbd.ttf");
    public static BaseFont DEFAULT_FONT_B;

    static {
        try {
            DEFAULT_FONT_R = BaseFont.createFont(R_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            DEFAULT_FONT_B = BaseFont.createFont(B_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e) {
            Logger.error("Error in font initialization", e);
        }
    }

    public ThankYouLetter(User user, ThankYouLetterFactory factory) {
        super(user, factory);
    }

    public String getLetterType() {
        return (String) user.getInfo().get("dm_ti_letters"); //TODO may be extract to configuration
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
        return ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "letter.jpg").getAbsolutePath();
    }

    @Override
    public boolean isHonored() {
        return getLetterType() != null && !getLetterType().equals("");
    }

    @Override
    public void draw(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(DEFAULT_FONT_R, 24);

        Info info = user.getInfo();
        String[] userFrom = getUserFrom(user);
        String schoolLine = userFrom[0];
        String addressLine = userFrom[1];
        String surnameName = String.format("%s %s %s",
                capitalize(info.get("surname")),
                capitalize(info.get("name")),
                capitalize(info.get("patronymic"))
        );

        float lineSkip = 18;

        canvas.setFontAndSize(DEFAULT_FONT_R, 14);
        canvas.showTextAligned(Element.ALIGN_RIGHT, "Директору " + schoolLine + ",", Utilities.millimetersToPoints(188), Utilities.millimetersToPoints(192), 0);
        canvas.showTextAligned(Element.ALIGN_RIGHT, "Школьному организатору Первой заочной", Utilities.millimetersToPoints(188), Utilities.millimetersToPoints(192) - lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_RIGHT, "дистанционной олимпиады по ДМ и ТИ", Utilities.millimetersToPoints(188), Utilities.millimetersToPoints(192) - 2 * lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_RIGHT, addressLine, Utilities.millimetersToPoints(188), Utilities.millimetersToPoints(192) - 3.5f * lineSkip, 0);

        float y0 = Utilities.millimetersToPoints(146);

        String dearWord = isMale(surnameName) ? "Уважаемый  " : "Уважаемая  ";
        float dearWidth = canvas.getEffectiveStringWidth(dearWord, true);
        canvas.setFontAndSize(DEFAULT_FONT_B, 14);
        float nameWidth = canvas.getEffectiveStringWidth(surnameName + "!", true);

        canvas.setFontAndSize(DEFAULT_FONT_R, 14);
        float x1 = Utilities.millimetersToPoints(105) - (dearWidth + nameWidth) / 2;
        canvas.showTextAligned(Element.ALIGN_LEFT, dearWord, x1, y0, 0);
        canvas.setFontAndSize(DEFAULT_FONT_B, 14);
        canvas.showTextAligned(Element.ALIGN_LEFT, surnameName + "!", x1 + dearWidth, y0, 0);

        canvas.setFontAndSize(DEFAULT_FONT_R, 14);

        String whatFor = "letter".equals(getLetterType()) ? "хорошие результаты" : "активное участие";

        //270 + 400 + 300
        canvas.showTextAligned(Element.ALIGN_CENTER, "Оргкомитет Первой олимпиады по дискретной математике", Utilities.millimetersToPoints(105), y0 - 2 * lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, "и теоретической информатике (ДМ и ТИ-2014) благодарит Вас", Utilities.millimetersToPoints(105), y0 - 3 * lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, "за " + whatFor + " Ваших учеников", Utilities.millimetersToPoints(105), y0 - 4 * lineSkip, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, "в заочном дистанционном туре олимпиады.", Utilities.millimetersToPoints(105), y0 - 5 * lineSkip, 0);

//        canvas.showTextAligned(Element.ALIGN_CENTER, "Уважаемый школьный организатор", Utilities.millimetersToPoints(105), y0 - 2 * lineSkip, 0);
//
//        canvas.setFontAndSize(DEFAULT_FONT_B, 14);
//        canvas.showTextAligned(Element.ALIGN_CENTER, surnameName, Utilities.millimetersToPoints(105), y0, 0);
//
//        canvas.setFontAndSize(DEFAULT_FONT_R, 14);
//        canvas.showTextAligned(Element.ALIGN_CENTER, schoolLine, Utilities.millimetersToPoints(105), y0 - lineSkip, 0);
//        canvas.showTextAligned(Element.ALIGN_CENTER, addressLine, Utilities.millimetersToPoints(105), y0 - 2 * lineSkip, 0);
//
//        canvas.showTextAligned(Element.ALIGN_CENTER, "Оргкомитет Первой олимпиады по дискретной математике", Utilities.millimetersToPoints(105), y0 - 4 * lineSkip, 0);
//        canvas.showTextAligned(Element.ALIGN_CENTER, "и теоретической информатике (ДМ и ТИ-2014) благодарит вас", Utilities.millimetersToPoints(105), y0 - 5 * lineSkip, 0);
//        canvas.showTextAligned(Element.ALIGN_CENTER, "и учеников вашей школы, принявших активное участие", Utilities.millimetersToPoints(105), y0 - 6 * lineSkip, 0);
//        canvas.showTextAligned(Element.ALIGN_CENTER, "в заочном дистанционном туре олимпиады", Utilities.millimetersToPoints(105), y0 - 7 * lineSkip, 0);
//        canvas.showTextAligned(Element.ALIGN_CENTER, "и достигших хороших результатов", Utilities.millimetersToPoints(105), y0 - 8 * lineSkip, 0);

                // и теоретической информатике (ДМ и ТИ-2014)
                //благодарит учеников Вашей школы, школьный организатор Днепровская Татьяна Владимировна, , принявших активное участие в заочном дистанционном туре олимпиады. ");

        /*if ("letter".equals(getLetterType())) {
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    String.format("ученики котор%s достигли хороших результатов", isMale(surnameName) ? "ого" : "ой"),
                    Utilities.millimetersToPoints(105), y0 - 4 * lineSkip, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, "в заочном дистанционном туре олимпиады.", Utilities.millimetersToPoints(105), y0 - 5 * lineSkip, 0);
        } else {
            canvas.showTextAligned(Element.ALIGN_CENTER,
                    String.format("ученики котор%s приняли активное участие", isMale(surnameName) ? "ого" : "ой"),
                    Utilities.millimetersToPoints(105), y0 - 4 * lineSkip, 0);
            canvas.showTextAligned(Element.ALIGN_CENTER, "в заочном дистанционном туре олимпиады.", Utilities.millimetersToPoints(105), y0 - 5 * lineSkip, 0);
        }*/

        canvas.endText();
        canvas.restoreState();
    }

    private boolean isMale(String surnameName) {
        return surnameName.endsWith("ч");
    }

    static String capitalize(Object s) {
        if (s == null || s.equals(""))
            return (String) s;
        String t = String.valueOf(s);
        return t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase();
    }

    //code taken from KioCertificate.drawUserFrom
    public static String[] getUserFrom(User user) {
        Info info = user.getInfo();
        String schoolLine = (String) info.get("school_name");
        String addressLine = (String) info.get("address");

        if (schoolLine == null && addressLine == null) {
            User regBy = user.getRegisteredByUser();

            if (regBy != null) {
                info = regBy.getInfo();
                schoolLine = (String) info.get("school_name");
                addressLine = (String) info.get("address");
            }
        }

        if (schoolLine == null)
            schoolLine = "";
        Object sendTo = info.get("send_to");
        if (addressLine == null || !"school".equals(sendTo))
            addressLine = "";

        String otherLine = (String) info.get("school_for_certificate");
        if (otherLine != null && !otherLine.isEmpty()) {
            schoolLine = otherLine;
            addressLine = "";
        }

        schoolLine = schoolLine.replaceAll("  ", " ").replaceAll("[\n\r]+", " ");
        addressLine = addressLine.replaceAll("  ", "").replaceAll("[\n\r]+", " ");

        return new String[]{schoolLine, addressLine};
    }
}
