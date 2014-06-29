package models.newserialization;

import java.util.Date;

/**
 * Created by ilya
 */
public class DateSerializationUtils {

    public static String dateToString(Date date) {
        return String.valueOf(date.getTime());
    }

    public static Date stringToDate(String date) {
        return new Date(Long.parseLong(date));
    }

}
