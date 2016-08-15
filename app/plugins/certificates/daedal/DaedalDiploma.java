package plugins.certificates.daedal;

import com.itextpdf.text.pdf.PdfWriter;
import models.User;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

/**
 * Project: dces2
 * Created by ilya on 13.08.16, 21:02.
 */
public class DaedalDiploma extends Diploma<DaedalDiplomaFactory> {
    public DaedalDiploma(User user, DaedalDiplomaFactory factory) {
        super(user, factory);
    }

    @Override
    public int getWidthsInMM() {
        return 0;
    }

    @Override
    public int getHeightInMM() {
        return 0;
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

    }
}
