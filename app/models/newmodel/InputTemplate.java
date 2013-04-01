package models.newmodel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 23:42
 */
public class InputTemplate {

    public static final Map<String, InputTemplate> registeredTemplates = new HashMap<>();

    static {

    }

    public static InputTemplate getInstance(String name) {
        return registeredTemplates.get(name);
    }

}
