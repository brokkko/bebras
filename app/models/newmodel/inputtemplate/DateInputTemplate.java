package models.newmodel.inputtemplate;

import models.newmodel.InputField;
import models.newmodel.RawForm;
import play.api.templates.Html;
import play.i18n.Messages;
import views.html.fields.date;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:24
 */
public class DateInputTemplate extends InputTemplate {

    @Override
    public Html format(RawForm form, InputField inputField) {
        return date.render(form, inputField.getName());
    }

    @Override
    public void write(String field, Object value, RawForm rawForm) {
        if (value == null) {
            rawForm.remove(field, "day");
            rawForm.remove(field, "month");
            rawForm.remove(field, "year");
            return;
        }

        GregorianCalendar date = new GregorianCalendar();
        date.setTime((Date) value);

        rawForm.put(field, date.get(GregorianCalendar.DAY_OF_MONTH), "day");
        rawForm.put(field, date.get(GregorianCalendar.MONTH), "month");
        rawForm.put(field, date.get(GregorianCalendar.YEAR), "year");
    }

    @Override
    public Object read(String field, RawForm form) {
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
}
