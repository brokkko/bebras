package models;

import controllers.Application;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.04.13
 * Time: 14:46
 */
public class Utils {

    public static final SimpleDateFormat createdDateTimeFormat = new SimpleDateFormat("YY.MM.dd HH:mm");
    public static final SimpleDateFormat contestDateFormat = new SimpleDateFormat("d MMMM YYYY, HH:mm");
    public static final SimpleDateFormat inputDateTimeFormat = new SimpleDateFormat("YYYY-MM-dd, HH:mm");

    public static <K, V> Map<K, V> mapify(Object... values) {
        Map<K, V> map = new HashMap<>();

        if (values.length % 2 != 0)
            throw new IllegalArgumentException("Number of arguments must be even");

        for (int i = 0; i < values.length; i += 2)
            //noinspection unchecked
            map.put((K) values[i], (V) values[i + 1]);

        return map;
    }

    @SafeVarargs
    public static <T> List<T> listify(T... values) {
        return Arrays.asList(values);
    }

    public static Date parseSimpleTime(String time) {
        if (time == null)
            return null;
        if (time.trim().isEmpty())
            return null;

        String[] items = time.split("[^0-9]+");

        if (items.length == 5)
            return new GregorianCalendar(
                    Integer.parseInt(items[0]),
                    Integer.parseInt(items[1]) - 1,
                    Integer.parseInt(items[2]),
                    Integer.parseInt(items[3]),
                    Integer.parseInt(items[4])
            ).getTime();
        else
            return new GregorianCalendar(
                    Integer.parseInt(items[0]),
                    Integer.parseInt(items[1]) - 1,
                    Integer.parseInt(items[2]),
                    Integer.parseInt(items[3]),
                    Integer.parseInt(items[4]),
                    Integer.parseInt(items[5])
            ).getTime();
    }

    public static String formatDateTimeForInput(Date date) {
        return inputDateTimeFormat.format(date);
    }

    public static String formatContestDate(Date date) {
        return contestDateFormat.format(date);
    }

    public static String formatObjectCreationTime(Date date) {
        return createdDateTimeFormat.format(date);
    }

    public static String scoresWord(int scores) {
        int s = Math.abs(scores);
        int a = s / 10 % 10;
        int b = s % 10;

        String word = "баллов";

        if (a != 1) {
            if (b == 1)
                word = "балл";
            else if (b == 2 || b == 3 || b == 4)
                word = "балла";
        }

        return scores + " " + word;
    }

    public static String millis2minAndSec(long time) {
        int seconds = (int) Math.round(time / 1000.0);

        int minutes = seconds / 60;
        seconds = seconds % 60;
        return toStringWithDigits(minutes, 2) + ":" + toStringWithDigits(seconds, 2);
    }

    private static String toStringWithDigits(int num, int digits) {
        String result = num + "";
        while (result.length() < digits)
            result = "0" + result;

        return result;
    }

    public static int compareStrings(String s1, String s2) {
        if (s1 == null)
            return s2 == null ? 0 : -1;
        if (s2 == null)
            return 1;

        int l1 = s1.length();
        int l2 = s2.length();

        //remove same symbols from the beginning that are not digits
        int skip = 0;
        while (skip < l1 && skip < l2 &&
                        s1.charAt(skip) == s2.charAt(skip) &&
                        !Character.isDigit(s1.charAt(skip)) && !Character.isDigit(s2.charAt(skip)))
            skip++;

        if (skip == l1)
            return skip == l2 ? 0 : -1;
        if (skip == l2)
            return 1;

        //read numbers
        int dSkip1 = skip; //substring(skip, dSkip) is a number
        int dSkip2 = skip;

        while (dSkip1 < l1 && Character.isDigit(s1.charAt(dSkip1)))
            dSkip1 ++;
        while (dSkip2 < l2 && Character.isDigit(s2.charAt(dSkip2)))
            dSkip2 ++;

        //if at least one of strings does not have digits
        if (dSkip1 == skip || dSkip2 == skip)
            return s1.charAt(skip) - s2.charAt(skip);

        int n1 = Integer.parseInt(s1.substring(skip, dSkip1));
        int n2 = Integer.parseInt(s2.substring(skip, dSkip2));

        if (n1 != n2)
            return n1 - n2;

        // handle 0-s in the beginning
        if (dSkip1 != dSkip2)
            return dSkip1 - dSkip2;

        return compareStrings(s1.substring(dSkip1), s2.substring(dSkip2)); //TODO rewrite as a cycle
    }

    public static String getResourceAsString(String name) throws IOException {
        InputStream inS = Application.class.getResourceAsStream(name);
        return inputStreamToString(inS);
    }

    public static String inputStreamToString(InputStream inS) throws IOException {
        BufferedReader inR = new BufferedReader(new InputStreamReader(inS, "UTF8"));
        CharArrayWriter out = new CharArrayWriter();
        int r;
        while ((r = inR.read()) >= 0)
            out.write(r);
        inR.close();
        return out.toString();
    }

    public static void writeResourceToFile(String resource, File file) throws IOException {
        writeResourceToFile(resource, file, null);
    }

    public static void writeResourceToFile(String resource, File file, Map<Object, Object> subs) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"))) {
            String resourceAsString = getResourceAsString(resource);

            if (subs != null)
                for (Object field : subs.keySet()) {
                    Object value = subs.get(field);
                    resourceAsString = resourceAsString.replace(String.valueOf(field), String.valueOf(value));
                }

            writer.write(resourceAsString);
        }
    }

    public static void runProcess(String... command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        int res = process.waitFor();
        if (res != 0)
            throw new IOException("Non zero exit code of " + command[0]);
    }

    public static String getExtension(String name) {
        if (name == null)
            return null;
        int pos = name.lastIndexOf('.');
        if (pos < 0)
            return null;
        return name.substring(pos + 1);
    }
}
