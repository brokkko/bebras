package models.problems;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.05.13
 * Time: 11:53
 */
public class ConfiguredProblem {

    //TODO problem configuration, i.e. the way to evaluate scores, etc
    private String link;
    private Problem problem;

    public ConfiguredProblem(String link, Problem problem) {
        if (!link.startsWith("/"))
            link = '/' + link;

        this.link = link;
        this.problem = problem;
    }

    public String getLink() {
        return link;
    }

    public Problem getProblem() {
        return problem;
    }

}
