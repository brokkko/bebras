package ru.ipo.daedal;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 1:33.
 */
public class DiplomaSettings {

    private Length width = new Length(210, Dimension.mm);
    private Length height = new Length(297, Dimension.mm);
    private String bg; //null for no bg

    public Length getWidth() {
        return width;
    }

    public void setWidth(Length width) {
        this.width = width;
    }

    public Length getHeight() {
        return height;
    }

    public void setHeight(Length height) {
        this.height = height;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public Document createDocument() {
        return new Document(
                new Rectangle(
                        width.getInPoints(), height.getInPoints()
                ),
                0, 0, 0, 0
        );
    }
}
