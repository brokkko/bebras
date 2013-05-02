package models;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.04.13
 * Time: 14:46
 */
public class Utils {

    public static final SimpleDateFormat contestDateFormat = new SimpleDateFormat("d MMMM YYYY, HH:mm");

    public static <K, V> Map<K, V> mapify(Object... values) {
        Map<K, V> map = new HashMap<>();

        if (values.length % 2 != 0)
            throw new IllegalArgumentException("Number of arguments must be even");

        for (int i = 0; i < values.length; i += 2)
            //noinspection unchecked
            map.put((K) values[i], (V) values[i + 1]);

        return map;
    }

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

    public static String formatContestDate(Date date) {
        return contestDateFormat.format(date);
    }

}
