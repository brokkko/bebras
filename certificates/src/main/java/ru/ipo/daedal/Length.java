package ru.ipo.daedal;


import com.itextpdf.text.Utilities;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 8:12.
 */
public class Length {

    public static Length parse(String s) {
        try {
            for (Dimension dimension : Dimension.values()) {
                String dim = dimension.toString();

                if (s.endsWith(dim)) {
                    s = s.substring(0, s.length() - dim.length());

                    return new Length(Float.parseFloat(s), dimension);
                }
            }

            return new Length(Float.parseFloat(s), Dimension.mm);
        } catch (NumberFormatException e) {
            throw new DaedalParserError("Wrong dimension: " + s);
        }
    }

    private float length;
    private Dimension dim;

    public Length(float length, Dimension dim) {
        this.length = length;
        this.dim = dim;
    }

    public float getLength() {
        return length;
    }

    public Dimension getDim() {
        return dim;
    }

    public float getInPoints() {
        switch (dim) {
            case mm:
                return Utilities.millimetersToPoints(length);
            case pt:
                return length;
            case in:
                return Utilities.inchesToPoints(length);
        }
        throw new DaedalParserError("Unknown dimension " + dim);
    }

    @Override
    public String toString() {
        return "" + length + dim;
    }
}
