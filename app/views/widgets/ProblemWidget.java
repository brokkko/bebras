package views.widgets;

import models.newproblems.Problem;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 11:34
 */
public class ProblemWidget implements Widget {

    public static ProblemWidget get(Problem problem) {
        return new ProblemWidget(problem);
    }

    private String problemType;

    public ProblemWidget(Problem problem) {
        problemType = problem.getType();
    }

    @Override
    public List<ResourceLink> links() {
        return Arrays.asList(new ResourceLink(problemType + ".problem", "js"), new ResourceLink(problemType + ".problem", "css"));
    }
}
