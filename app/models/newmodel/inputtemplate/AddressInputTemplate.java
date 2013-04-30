package models.newmodel.inputtemplate;

import models.Address;
import models.newmodel.InputField;
import models.newmodel.RawForm;
import play.api.templates.Html;
import play.i18n.Messages;
import views.html.fields.address;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 18.01.13
 * Time: 20:31
 */
public class AddressInputTemplate extends InputTemplate {

    @Override
    public Html format(RawForm form, InputField inputField) {
        return address.render(form, inputField.getName());
    }

    @Override
    public void write(String field, Object value, RawForm rawForm) {
        if (value == null) {
            rawForm.remove(field, "index");
            rawForm.remove(field, "city");
            rawForm.remove(field, "street");
            rawForm.remove(field, "house");
            return;
        }

        Address addr = (Address) value;

        rawForm.put(field, addr.getIndex(), "index");
        rawForm.put(field, addr.getCity(), "city");
        rawForm.put(field, addr.getStreet(), "street");
        rawForm.put(field, addr.getHouse(), "house");
    }

    @Override
    public Object read(String field, RawForm form) {
        if (
                form.isEmptyValue(field, "index") &&
                form.isEmptyValue(field, "city") &&
                form.isEmptyValue(field, "street") &&
                form.isEmptyValue(field, "house")
        )
            return null;

        if (form.isEmptyValue(field, "index"))
            form.reject(field, Messages.get("error.msg.addr.no_index"));
        if (form.isEmptyValue(field, "city"))
            form.reject(field, Messages.get("error.msg.addr.no_city"));
        if (form.isEmptyValue(field, "street"))
            form.reject(field, Messages.get("error.msg.addr.no_street"));
        if (form.isEmptyValue(field, "house"))
            form.reject(field, Messages.get("error.msg.addr.no_house"));

        String index = form.get(field, "index");
        String city = form.get(field, "city");
        String street = form.get(field, "street");
        String house = form.get(field, "house");

        if (index != null && !index.matches("\\d{6}"))
            form.reject(field, Messages.get("error.msg.addr.wrong_index"));

        if (form.hasFieldErrors(field))
            return null;

        return new Address(index, city, street, house);
    }
}
