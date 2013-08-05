package models.forms.inputtemplate;

import models.Address;
import models.forms.RawForm;
import models.newserialization.SerializableSerializationType;
import models.newserialization.SerializationType;
import play.api.templates.Html;
import play.i18n.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 18.01.13
 * Time: 20:31
 */
public class AddressInputTemplate extends InputTemplate<Address> {

    @Override
    public Html render(RawForm form, String field) {
        return views.html.fields.address.render(form, field);
    }

    @Override
    public void write(String field, Address value, RawForm rawForm) {
        if (value == null) {
            rawForm.remove(field, "index");
            rawForm.remove(field, "city");
            rawForm.remove(field, "street");
            rawForm.remove(field, "house");
            return;
        }

        rawForm.put(field, value.getIndex(), "index");
        rawForm.put(field, value.getCity(), "city");
        rawForm.put(field, value.getStreet(), "street");
        rawForm.put(field, value.getHouse(), "house");
    }

    @Override
    public Address read(String field, RawForm form) {
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

    @Override
    public SerializationType<Address> getType() {
        return new SerializableSerializationType<>(Address.class);
    }

    @Override
    public String[] getUserInputFields() {
        return new String[]{"index", "city", "street", "house"};
    }
}
