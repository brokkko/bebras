package models;

import play.data.validation.Constraints;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * OldeUser: ilya
 * Date: 26.12.12
 * Time: 23:13
 */
public class OldeUser {

    @Constraints.Required
    private String login;

    private String passwordHash;

    @Constraints.Required
    private String name;

    @Constraints.Required
    private String surname;

    @Constraints.Required
    private String patronymic;

    private Date birthday;

    private String phone;

    @Constraints.Email
    @Constraints.Required
    private String email;

    @Constraints.Pattern("\\d{8}")
    @Constraints.Required
    private String schoolCode;

    @Constraints.Required
    private String schoolName;

    public String validate() {
        return "";
    }

}
