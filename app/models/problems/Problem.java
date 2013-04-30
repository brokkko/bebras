package models.problems;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 11:09
 */
public class Problem {

    public static final String STATEMENT = "statement";
    public static final String SOLUTION = "solution";
    public static final String CHECKER = "checker";

    /*public Problem() {
        super(new MemoryDeserializer());
    }

    public Problem(StoredObject storedObject) {
        super(storedObject);
    }

    public String getStatement() {
        return (String) get(STATEMENT);
    }

    public String getSolution() {
        return (String) get(SOLUTION);
    }

    public Checker getChecker() {
        return Checker.getInstance(getString(CHECKER));
    }

    public Problem generate() {
        return generate(0);
    }

    public Problem generate(long userId) {
        return this;
    }*/

}
