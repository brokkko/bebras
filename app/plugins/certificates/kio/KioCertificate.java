package plugins.certificates.kio;

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
import java.util.ArrayList;
import java.util.List;

public class KioCertificate extends Diploma<KioCertificateFactory> {

    public static final String PLUGIN_NAME = "KioDiplomas";
    public static final File R_FONT_FILE = ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "AmbassadoreType.ttf");
    public static BaseFont DEFAULT_FONT_R;
    public static final File I_FONT_FILE = ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "AmbassadoreType Italic.ttf");
    public static BaseFont DEFAULT_FONT_I;
    public static final File R_RESULTS_FONT_FILE = ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "Arial-R.ttf");
    public static BaseFont RESULTS_FONT_R;
    public static final File B_RESULTS_FONT_FILE = ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "Arial-B.ttf");
    public static BaseFont RESULTS_FONT_B;

    static {
        try {
            DEFAULT_FONT_R = BaseFont.createFont(R_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            DEFAULT_FONT_I = BaseFont.createFont(I_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            RESULTS_FONT_R = BaseFont.createFont(R_RESULTS_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            RESULTS_FONT_B = BaseFont.createFont(B_RESULTS_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e) {
            Logger.error("Error in font initialization", e);
        }
    }

    public KioCertificate(User user, KioCertificateFactory factory) {
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
        return ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, "Certificate.png").toString();
    }

    @Override
    public boolean isHonored() {
        return true;
    }

    private int getLevel() {
        try {
            String level = getResult("level");
            if (level == null || level.isEmpty())
                level = getResult("kiolevel");
            return Integer.parseInt(level);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getLevelAsString() {
        switch (getLevel()) {
            case 0:
                return "0";
            case 1:
                return "I";
            case 2:
                return "II";
        }
        return "0";
    }

    private String surnameName() {
        return surnameName(user);
    }

    public static String surnameName(User user) {
        return String.format("%s %s", user.getInfo().get("surname"), user.getInfo().get("name")).toUpperCase();
    }

    public static void drawUserFrom(PdfContentByte canvas, User user, float y0) {
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

        canvas.setFontAndSize(DEFAULT_FONT_R, 12);
        canvas.showTextAligned(Element.ALIGN_CENTER, schoolLine, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0), 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, addressLine, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(y0 - 5), 0);
    }

    private List<KioProblemDescription> getProblems() {
        List<Integer> problemsForLevel = factory.getProblemsByLevels().get(getLevel());
        List<KioProblemDescription> problems = new ArrayList<>(problemsForLevel.size());

        for (Integer problemId : problemsForLevel)
            problems.add(factory.getProblems().get(problemId));

        return problems;
    }

    private boolean scoresAreNonZero(String scores) {
        return scores != null && !"0".equals(scores) && !"".equals(scores) && !"-".equals(scores);
    }

    private String getScoresForProblem(KioProblemDescription problemDescription) {
        String scoresField = problemDescription.getScoresField();
        return getResult(scoresField, problemDescription.getProblemContestId());
    }

    private boolean hasAtLeastOneSolution() {
        for (KioProblemDescription problemDescription : getProblems())
            if (scoresAreNonZero(getScoresForProblem(problemDescription)))
                return true;

        return false;
    }

    @Override
    public void draw(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.beginText();

        canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

        canvas.setFontAndSize(DEFAULT_FONT_R, 24);
        canvas.showTextAligned(Element.ALIGN_CENTER, surnameName(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(161), 0);

        drawUserFrom(canvas, user, 156);

        canvas.setFontAndSize(DEFAULT_FONT_R, 17);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Санкт-Петербург " + factory.getYear(), Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(3), 0);

        if (hasAtLeastOneSolution()) {
            String levelInfo = "и занял(а) в рейтинге ( " + getLevelAsString() + " уровень )";
            canvas.showTextAligned(Element.ALIGN_LEFT, levelInfo, Utilities.millimetersToPoints(60), Utilities.millimetersToPoints(124), 0);

            int level = getLevel();
            String placeInfo = String.format("%s место из %s", getResult("rank", null), factory.getParticipantsByLevels().get(level));
            canvas.showTextAligned(Element.ALIGN_CENTER, placeInfo, Utilities.millimetersToPoints(105), Utilities.millimetersToPoints(117), 0);

            outputProblemsResult(canvas);
        }

        canvas.endText();
        canvas.restoreState();
    }

    private void outputProblemsResult(PdfContentByte canvas) {
        canvas.setFontAndSize(RESULTS_FONT_B, 11);

        float y0 = 111;
        float lineSkip = 4.7f;
        float x0 = 19;
        float x1 = 141 + factory.getResultsShiftInMM();

        for (KioProblemDescription problemDescription : getProblems()) {
            String title = String.format("Результат в задаче «%s»", problemDescription.getName());
            canvas.showTextAligned(Element.ALIGN_LEFT, title, Utilities.millimetersToPoints(x0), Utilities.millimetersToPoints(y0), 0);

            List<String> fields = problemDescription.getFields();
            String[] fieldsValues = new String[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                String field = fields.get(i);
                fieldsValues[i] = getResult(field, problemDescription.getProblemContestId());
            }

            String rankField = problemDescription.getRankField();
            String rank = getResult(rankField, problemDescription.getProblemContestId());
            String scores = getScoresForProblem(problemDescription);

            String resultsFormat = problemDescription.getPattern();
            String resultsText = String.format(resultsFormat, (Object[]) fieldsValues);
            if (!resultsText.contains("\n"))
                resultsText = String.format("%s место\n(%s)", rank, resultsText);
            else
                resultsText = String.format("%s место (%s)", rank, resultsText);

            if (!scoresAreNonZero(scores))
                resultsText = "-";

            int breakIndex = resultsText.indexOf('\n');
            String line1 = breakIndex < 0 ? resultsText : resultsText.substring(0, breakIndex);
            String line2 = breakIndex < 0 ? "" : resultsText.substring(breakIndex + 1);


            canvas.showTextAligned(Element.ALIGN_LEFT, line1, Utilities.millimetersToPoints(x1), Utilities.millimetersToPoints(y0), 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, line2, Utilities.millimetersToPoints(x1), Utilities.millimetersToPoints(y0 - lineSkip), 0);

            y0 -= 2.3 * lineSkip;
        }
    }
}
