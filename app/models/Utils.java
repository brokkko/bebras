package models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 30.04.13
 * Time: 14:46
 */
public class Utils {

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

}
