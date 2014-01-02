package plugins.certificates;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import models.ServerConfiguration;
import play.Logger;

import java.io.File;
import java.io.IOException;

public class CertificateLine {

    public static final File R_FONT_FILE = ServerConfiguration.getInstance().getResource("Ubuntu-R.ttf");
    public static BaseFont DEFAULT_FONT_R;
    public static final File B_FONT_FILE = ServerConfiguration.getInstance().getResource("Ubuntu-B.ttf");
    public static BaseFont DEFAULT_FONT_B;

    static {
        try {
            DEFAULT_FONT_R = BaseFont.createFont(R_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            DEFAULT_FONT_B = BaseFont.createFont(B_FONT_FILE.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e) {
            Logger.error("Error in font initialization", e);
        }
    }

    private String line;
    private float size;
    private boolean bold;

    public CertificateLine(String line, float size, boolean bold) {
        this.line = line;
        this.size = size;
        this.bold = bold;
    }

    public String getLine() {
        return line;
    }

    public Font getFont() {
        return new Font(getBaseFont(), size, Font.NORMAL);
    }

    public BaseFont getBaseFont() {
        return bold ? DEFAULT_FONT_B : DEFAULT_FONT_R;
    }

    public float getSize() {
        return size;
    }
}
