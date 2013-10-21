package models.forms.inputtemplate;

import models.utils.Utils;
import models.forms.RawForm;
import models.newserialization.BasicSerializationType;
import models.newserialization.SerializationType;
import play.api.templates.Html;
import play.i18n.Messages;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.07.13
 * Time: 17:17
 */
public class DateTimeInputTemplate extends InputTemplate<Date> {

    @Override
    public Html render(RawForm form, String field) {
        return views.html.fields.datetime.render(form, field);
    }

    @Override
    public void write(String field, Date value, RawForm rawForm) {
        if (value != null)
            rawForm.put(field, Utils.formatDateTimeForInput(value));
    }

    @Override
    public Date read(String field, RawForm form) {
        String value = form.get(field);
        if (value == null)
            return null;

        try {
            return Utils.parseSimpleTime(value);
        } catch (NumberFormatException nfe) {
            form.reject(field, Messages.get("error.msg.datetime"));
            return null;
        }
    }

    @Override
    public SerializationType<Date> getType() {
        return new BasicSerializationType<>(Date.class);
    }
}
