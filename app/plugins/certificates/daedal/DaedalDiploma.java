package plugins.certificates.daedal;

import com.itextpdf.text.pdf.PdfWriter;
import models.ServerConfiguration;
import models.User;
import plugins.certificates.Diploma;
import ru.ipo.daedal.Context;
import ru.ipo.daedal.DaedalParser;
import ru.ipo.daedal.ExpressionEvaluator;
import ru.ipo.daedal.commands.compiler.CompilerContext;

/**
 * Project: dces2
 * Created by ilya on 13.08.16, 21:02.
 */
public class DaedalDiploma extends Diploma<DaedalDiplomaFactory> {

    private CompilerContext cc;

    public DaedalDiploma(User user, DaedalDiplomaFactory factory) {
        super(user, factory);
        ExpressionEvaluator evaluator = e -> {
            String result = "???";
            switch (e) {
                case "FULL NAME": result = user.getFullName().toUpperCase(); break;
                case "school": result = (String) user.getInfo().get("school_name"); break;
                case "address": result = (String) user.getInfo().get("address"); break;
            }
            return result == null ? "" : result;
        };
        DaedalParser parser = new DaedalParser(evaluator);
        try {
            cc = parser.parse(factory.getDefinition());
        } catch (Exception e) {
            e.printStackTrace();
            cc = null;
        }
    }

    @Override
    public int getWidthsInMM() {
        return (int) cc.getDiplomaSettings().getWidth().getInMM();
    }

    @Override
    public int getHeightInMM() {
        return (int) cc.getDiplomaSettings().getHeight().getInMM();
    }

    @Override
    public String bgPath() {
        return ServerConfiguration.getInstance().getPluginFile(
                DaedalDiplomaFactory.PLUGIN_NAME,
                cc.getDiplomaSettings().getBg()
        ).getAbsolutePath();
    }

    @Override
    public boolean isHonored() {
        return true;
    }

    @Override
    public void draw(PdfWriter writer) {
        Context context = new Context(cc.getDiplomaSettings(), null, writer, ServerConfiguration.getInstance().getPluginFolder(DaedalDiplomaFactory.PLUGIN_NAME));
        cc.execInstructions(context);
    }
}
