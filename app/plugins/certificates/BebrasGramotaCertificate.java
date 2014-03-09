package plugins.certificates;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import models.Event;
import models.ServerConfiguration;
import models.User;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BebrasGramotaCertificate extends Certificate {
    public static final File R_ARIAL_FONT_FILE = ServerConfiguration.getInstance().getResource("Arial-R.ttf");
    public static BaseFont ARIAL_FONT_R;
    public static final File B_ARIAL_FONT_FILE = ServerConfiguration.getInstance().getResource("Arial-B.ttf");
    public static BaseFont ARIAL_FONT_B;

    static {
        try {
            ARIAL_FONT_R = BaseFont.createFont(R_ARIAL_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            ARIAL_FONT_B = BaseFont.createFont(B_ARIAL_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e) {
            Logger.error("Error in font initialization", e);
        }
    }

    private boolean isActive;
    private List<CertificateLine> schoolAddrLines;

    public BebrasGramotaCertificate(User user, boolean isActive, List<CertificateLine> schoolAddrLines) {
        super(user);
        this.isActive = isActive;
        this.schoolAddrLines = schoolAddrLines;
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
        return Event.current().getEventDataFolder().getAbsolutePath() + "/teacher_gramota.png";
    }

    @Override
    public void draw(PdfWriter writer) {
        try {

            float x0 = 105;
            float y0 = 20;

            float lineSkip = 1.5f;

            boolean firstLine = true;
//            for (CertificateLine line : schoolAddrLines) {
//                if (firstLine)
//                    firstLine = false;
//                else
//                    y0 -= Utilities.pointsToMillimeters(line.getSize() * lineSkip);
//                printDiplomGramotaText(writer, line.getLine(), 14, false, x0, y0);
//            }
            formatDiplomGramotaHeader(writer);

            formatGramotaData(writer);

            formatDiplomGramotaFooter(writer);

        } catch (Exception e) {
            Logger.error("failed to write text", e);
        }
    }

    private void formatGramotaData(PdfWriter writer) throws IOException {
//        formatDiplomGramotaHeader();

        printDiplomGramotaText(
                writer, ((String)user.getInfo().get("surname")).toUpperCase() + " " +
                ((String)user.getInfo().get("name")).toUpperCase() + " " +
                ((String)user.getInfo().get("patronymic")).toUpperCase(),
                18, true, 216 / 2, 154 + 5
        );
        float x0 = 216 / 2;
        float y0 = 144 + 9;
        float size = 12;
        float lineSkip = Utilities.pointsToMillimeters(14) * 1.3f;

        printDiplomGramotaText(writer, "школьный организатор", size, false, x0, y0);
        y0 -= lineSkip;
        for (CertificateLine schoolAddrLine : schoolAddrLines) {
            printDiplomGramotaText(writer, schoolAddrLine.getLine(), size, false, x0, y0);
            y0 -= lineSkip;
        }
        printDiplomGramotaText(writer, "за активное участие в подготовке и проведении", size, false, x0, y0);
        y0 -= lineSkip;
        printDiplomGramotaText(writer, "Международного конкурса по информатике «Бобер-2013»", size, false, x0, y0);
        if (isActive) {
            y0 -= lineSkip;
            if (((String)user.getInfo().get("patronymic")).toUpperCase().endsWith("Ч"))
                printDiplomGramotaText(writer, "и вошедший в число лучших организаторов по России", size, false, x0, y0);
            else
                printDiplomGramotaText(writer, "и вошедшая в число лучших организаторов по России", size, false, x0, y0);
        }
    }

    private static void formatDiplomGramotaFooter(PdfWriter writer) throws IOException {
        float y0 = 101;
        float x0 = 216 / 2;
        float size = 11;
        float lineSkipSmall = Utilities.pointsToMillimeters(size) * 1.5f;
        float lineSkipBig = 4.9f + lineSkipSmall;
        printDiplomGramotaText(writer, "ЧЛЕНЫ ЖЮРИ:", size, false, x0, y0);
        y0 -= lineSkipBig / 1.4f;

        printDiplomGramotaText(writer, "Председатель Организационного комитета  конкурса «Бобёр-2012»,", size, false, x0, y0);
        y0 -= lineSkipSmall;
        printDiplomGramotaText(writer, "декан факультета компьютерных технологий и информатики СПбГЭТУ «ЛЭТИ», д.т.н.", size, false, x0, y0);
        y0 -= lineSkipSmall * 1.4f;
        printDiplomGramotaText(writer, "М. С. Куприянов", size, false, x0 + 50, y0, false);
        y0 -= lineSkipBig;

        printDiplomGramotaText(writer, "Директор Инновационного института продуктивного обучения СЗО РАО,", size, false, x0, y0);
        y0 -= lineSkipSmall;
        printDiplomGramotaText(writer, "академик РАО", size, false, x0, y0);
        y0 -= lineSkipSmall * 1.4f;
        printDiplomGramotaText(writer, "М. И. Башмаков", size, false, x0 + 50, y0, false);
        y0 -= lineSkipBig;

        printDiplomGramotaText(writer, "Главный редактор журнала «Компьютерные инструменты в образовании»,", size, false, x0, y0);
        y0 -= lineSkipSmall;
        printDiplomGramotaText(writer, "научный руководитель конкурса «Бобёр-2012», д.п.н.", size, false, x0, y0);
        y0 -= lineSkipSmall * 1.4f;
        printDiplomGramotaText(writer, "С. Н. Поздняков", size, false, x0 + 50, y0, false);
    }

    private static void formatDiplomGramotaHeader(PdfWriter writer) throws IOException {
        printDiplomGramotaText(writer, "награждается", 12, false, 216 / 2, 165 + 1);
    }

    private static void printDiplomGramotaText(PdfWriter writer, String text, float size, boolean isBold, float x0, float y0) throws IOException {
        printDiplomGramotaText(writer, text, size, isBold, x0, y0, true);
    }

    private static void printDiplomGramotaText(PdfWriter writer, String text, float size, boolean isBold, float x0, float y0, boolean center) throws IOException {
        BaseFont baseFont = isBold ? ARIAL_FONT_B : ARIAL_FONT_R;

        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setFontAndSize(baseFont, size);
        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);
        canvas.showTextAligned(center ? Element.ALIGN_CENTER : Element.ALIGN_LEFT, text, Utilities.millimetersToPoints(x0), Utilities.millimetersToPoints(y0), 0);

        canvas.endText();
        canvas.restoreState();
    }
}
