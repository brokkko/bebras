package plugins.certificates.kio;

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

    }
}
