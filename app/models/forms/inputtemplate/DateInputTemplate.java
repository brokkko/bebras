package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newserialization.BasicSerializationType;
import models.newserialization.SerializationType;
import play.api.templates.Html;
import play.i18n.Messages;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:24
 */
public class DateInputTemplate extends InputTemplate<Date> {

    @Override
    public Html render(RawForm form, String field) {
        return views.html.fields.date.render(form, field);
    }

    @Override
    public void write(String field, Date value, RawForm rawForm) {
        if (value == null) {
            rawForm.remove(field, "day");
            rawForm.remove(field, "month");
            rawForm.remove(field, "year");
            return;
        }

        GregorianCalendar date = new GregorianCalendar();
        date.setTime(value);

        rawForm.put(field, date.get(GregorianCalendar.DAY_OF_MONTH), "day");
        rawForm.put(field, date.get(GregorianCalendar.MONTH), "month");
        rawForm.put(field, date.get(GregorianCalendar.YEAR), "year");
    }

    @Override
    public Date read(String field, RawForm form) {
        if (form.isEmptyValue(field, "day") && form.isEmptyValue(field, "month") && form.isEmptyValue(field, "year"))
            return null;

        int day = form.getAsInt(field, -1, "day");
        int month = form.getAsInt(field, -1, "month");
        int year = form.getAsInt(field, -1, "year");

        if (day < 0)
            form.reject(field, Messages.get("error.msg.date.no_day"));
        if (month < 0)
            form.reject(field, Messages.get("error.msg.date.no_month"));
        if (year < 0)
            form.reject(field, Messages.get("error.msg.date.no_year"));

        if (form.hasFieldErrors(field))
            return null;

        @SuppressWarnings("ConstantConditions")
        GregorianCalendar cal = new GregorianCalendar(year, month, day);
        cal.setLenient(false);

        try {
            return cal.getTime();
        } catch (IllegalArgumentException e) {
            form.reject(field, Messages.get("error.msg.date.not_exists"));
            return null;
        }
    }

    @Override
    public SerializationType<Date> getType() {
        return new BasicSerializationType<>(Date.class);
    }

}
