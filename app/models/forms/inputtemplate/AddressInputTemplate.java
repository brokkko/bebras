package models.forms.inputtemplate;

import models.Address;
import models.store.StoredObject;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.i18n.Messages;
import views.html.fields.address;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 18.01.13
 * Time: 20:31
 */
public class AddressInputTemplate extends InputTemplate {

    @Override
    public Html format(DynamicForm form, String field, InputTemplateConfig config) {
        return address.render(form, field);
    }

    @Override
    public BindResult getObject(DynamicForm form, String field) {
        String index = form.field(field + "[index]").value();
        String city = form.field(field + "[city]").value();
        String street = form.field(field + "[street]").value();
        String house = form.field(field + "[house]").value();

        if (empty(index) && empty(city) && empty(street) && empty(house))
            return new BindResult(null);

        List<String> messages = new ArrayList<>();

        if (empty(index))
            messages.add(Messages.get("error.msg.addr.no_index"));
        if (empty(city))
            messages.add(Messages.get("error.msg.addr.no_city"));
        if (empty(street))
            messages.add(Messages.get("error.msg.addr.no_street"));
        if (empty(house))
            messages.add(Messages.get("error.msg.addr.no_house"));

        if (index != null && ! index.matches("\\d{6}"))
            messages.add(Messages.get("error.msg.addr.wrong_index"));

        if (messages.size() > 0)
            return new BindResult(null, messages);

        Address result = new Address(index, city, street, house);
        return new BindResult(result);
    }

    @Override
    public void fillForm(DynamicForm form, String field, Object value) {
        if (value == null) {
            removeFormField(form, field + "[index]");
            removeFormField(form, field + "[city]");
            removeFormField(form, field + "[street]");
            removeFormField(form, field + "[house]");
            return;
        }

        @SuppressWarnings("unchecked")
        Address addr = smthToAddress(value);

        setFormField(form, field + "[index]", "" + addr.getIndex());
        setFormField(form, field + "[city]", "" + addr.getCity());
        setFormField(form, field + "[street]", "" + addr.getStreet());
        setFormField(form, field + "[house]", "" + addr.getHouse());
    }

    private Address smthToAddress(Object o) { //TODO think how to properly store such object
        if (o instanceof Address)
            return (Address) o;
        else if (o instanceof Map)
            return new Address(
                    (String) ((Map) o).get(Address.INDEX),
                    (String) ((Map) o).get(Address.CITY),
                    (String) ((Map) o).get(Address.STREET),
                    (String) ((Map) o).get(Address.HOUSE)
            );
        else if (o instanceof StoredObject)
            return new Address(
                    ((StoredObject) o).getString(Address.INDEX),
                    ((StoredObject) o).getString(Address.CITY),
                    ((StoredObject) o).getString(Address.STREET),
                    ((StoredObject) o).getString(Address.HOUSE)
            );

        return null;
    }

    private boolean empty(String s) {
        return s == null || s.isEmpty();
    }

}
