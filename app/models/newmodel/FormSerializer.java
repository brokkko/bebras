package models.newmodel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 12:48
 */
public class FormSerializer implements Serializer {

    private Map<String, String> map = new HashMap<>();
    private InputForm form;

    @Override
    public void write(String field, Object value) {
        InputField inputField = form.getField(field);
        if (inputField == null) //don't serialize values that are not in the specification
            return;

    }

    @Override
    public Serializer getSerializer(String field) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ListSerializer getListSerializer(String field) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
