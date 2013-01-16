package models.forms.inputtemplate;

import play.api.templates.Html;
import play.data.DynamicForm;
import play.i18n.Messages;
import views.html.fields.date;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:24
 */
public class DateInputTemplate extends InputTemplate {

    @Override
    public Html format(DynamicForm form, String field, InputTemplateConfig config) {
        return date.render(form, field);
    }

    @Override
    public BindResult getObject(DynamicForm form, String field) {
        String dayS = form.field(field + "[day]").value();
        String monthS = form.field(field + "[month]").value();
        String yearS = form.field(field + "[year]").value();

        Integer day = str2int(dayS);
        Integer month = str2int(monthS);
        Integer year = str2int(yearS);

        if (
                (dayS == null || dayS.isEmpty()) &&
                (monthS == null || monthS.isEmpty()) &&
                (yearS == null || yearS.isEmpty())
        )
            return new BindResult(null);

        List<String> messages = new ArrayList<>();

        if (day == null)
            messages.add(Messages.get("error.msg.date.no_day"));
        if (month == null)
            messages.add(Messages.get("error.msg.date.no_month"));
        if (year == null)
            messages.add(Messages.get("error.msg.date.no_year"));

        if (messages.size() > 0)
            return new BindResult(null, messages);

        @SuppressWarnings("ConstantConditions")
        GregorianCalendar cal = new GregorianCalendar(year, month, day);
        cal.setLenient(false);
        Date result;
        try {
            result = cal.getTime();
        } catch (IllegalArgumentException e) {
            return new BindResult(null, Messages.get("error.msg.date.not_exists"));
        }

        return new BindResult(result);
    }

    @Override
    public void fillForm(DynamicForm form, String field, Object value) {
        if (value == null)
            return;

        GregorianCalendar date = new GregorianCalendar();
        date.setTime((Date) value);

        setFormField(form, field + "[day]", "" + date.get(GregorianCalendar.DAY_OF_MONTH));
        setFormField(form, field + "[month]", "" + date.get(GregorianCalendar.MONTH));
        setFormField(form, field + "[year]", "" + date.get(GregorianCalendar.YEAR));
    }

    private Integer str2int(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
