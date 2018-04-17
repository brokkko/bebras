package models.results;

import models.newserialization.JSONSerializer;

import java.util.HashMap;

/**
 * Created by ilya
 */
public class Info extends HashMap<String, Object> {

    public Info(Object... values) {
        int count = values.length;
        if (count % 2 != 0)
            throw new IllegalArgumentException("Number of arguments should be even");

        for (int i = 0; i < count; i += 2)
            put((String) values[i], values[i + 1]);
    }
}
