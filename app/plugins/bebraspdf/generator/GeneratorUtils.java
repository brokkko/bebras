package plugins.bebraspdf.generator;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;

import java.io.IOException;

/**
 * @author Vasiliy
 * @date 18.10.13
 */
public class GeneratorUtils {

    public static final int DEFAULT_FONT_SIZE=14;

    private GeneratorUtils(){};

    public static BaseFont getBaseFont(){
        BaseFont times = null;
        try {
            times = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", "cp1251", BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return times;
    }

}
