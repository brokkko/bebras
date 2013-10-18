package plugins.bebraspdf.model.enums;

/**
 * Список классов пользователей
 *
 * @author Vasiliy
 * @date 18.10.13
 */
public enum UserClass {
    FIRST("1", 1),
    SECOND("2", 2),
    THIRD("3", 3),
    FOURTH("4", 4),
    FIFTH("5", 5),
    SIXTH("6", 6),
    SEVENTH("7", 7),
    EIGHTN("8", 8),
    NINHT("9", 9),
    TENTH("10", 10),
    ELEVENTH("11", 11);

    private final String name;

    private final int classNumber;

    UserClass(String name, int classNumber) {
        this.name = name;
        this.classNumber = classNumber;
    }

    public int getClassNumber() {
        return classNumber;
    }

    public String getName() {
        return name;
    }

    public static UserClass getUserClassByClassNumber(int classNumber) {
        for (UserClass userClass : UserClass.values()) {
            if (userClass.getClassNumber() == classNumber) {
                return userClass;
            }
        }
        return null;
    }
}
