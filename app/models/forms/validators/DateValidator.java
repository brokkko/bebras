package models.forms.validators;

import models.newserialization.Deserializer;
import models.newserialization.Serializer;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.01.13
 * Time: 17:00
 */
public class DateValidator extends Validator<Date> {

    private String comparison;

    @Override
    public Validator.ValidationResult validate(Date date) {

        Pattern pattern = Pattern.compile("(<|<=|>|>=|=)\\s*now\\s*(\\+|\\-)\\s*(\\d+)\\s*([YMwdhms])");

        Matcher matcher = pattern.matcher(comparison);
        if (! matcher.matches())
            throw new IllegalArgumentException("Comparison specification syntax error");

        String comparisonType = matcher.group(1);
        String additionSign = matcher.group(2);
        String amountS = matcher.group(3);
        String dimension = matcher.group(4);

        GregorianCalendar compareTo = new GregorianCalendar();

        int amount;
        try {
            amount = Integer.parseInt(amountS);
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Failed to read amount");
        }

        if (additionSign.equals("-"))
            amount = - amount;

        switch (dimension) {
            case "Y":
                compareTo.add(GregorianCalendar.YEAR, amount);
                break;
            case "M":
                compareTo.add(GregorianCalendar.MONTH, amount);
                break;
            case "w":
                compareTo.add(GregorianCalendar.WEEK_OF_YEAR, amount);
                break;
            case "d":
                compareTo.add(GregorianCalendar.DAY_OF_MONTH, amount);
                break;
            case "h":
                compareTo.add(GregorianCalendar.HOUR, amount);
                break;
            case "m":
                compareTo.add(GregorianCalendar.MINUTE, amount);
                break;
            case "s":
                compareTo.add(GregorianCalendar.SECOND, amount);
                break;
        }

        Date compareToDate = compareTo.getTime();

        boolean ok = false;
        switch (comparisonType) {
            case ">":
                ok = date.compareTo(compareToDate) > 0;
                break;
            case "<":
                ok = date.compareTo(compareToDate) < 0;
                break;
            case ">=":
                ok = date.compareTo(compareToDate) >= 0; //TODO to test all equalities
                break;
            case "<=":
                ok = date.compareTo(compareToDate) <= 0;
                break;
            case "=":
                ok = date.compareTo(compareToDate) == 0;
                break;
        }

        return ok ? ok() : message();
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        comparison = deserializer.readString("comparison");
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("comparison", comparison);
    }
}
