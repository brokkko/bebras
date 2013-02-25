package models.problems;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 13:59
 */
public interface ProblemsSource {

    //TODO report a bug that if interface method is called get, then it is not detected as not used
    Problem get(String id);

}
