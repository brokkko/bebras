package ru.ipo.daedal;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 11:26.
 */
public class ResourceReader {

    public static String inputStreamToString(InputStream is) {
        try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    public static String resourceToString(String name) {
        InputStream is = ResourceReader.class.getResourceAsStream(name);
        return inputStreamToString(is);
    }
}
