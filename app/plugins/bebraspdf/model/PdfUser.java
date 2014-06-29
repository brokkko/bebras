package plugins.bebraspdf.model;

/**
 * Бин для хранения участника соревнований
 * @author Vasiliy
 * @date 18.10.13
 */
public class PdfUser {

    private String name;

    private String surname;

    public PdfUser(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}
