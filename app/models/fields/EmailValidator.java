package models.fields;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.01.13
 * Time: 17:57
 */
public class EmailValidator extends InputValidator {
    protected EmailValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
    }

    @Override
    public String validate(Object value) {
//        InternetAddress internetAdd = new InternetAddress("test@test.com");
        return null; //TODO implement, use http://mvnrepository.com/artifact/org.apache.commons/commons-email/1.2
    }
}
