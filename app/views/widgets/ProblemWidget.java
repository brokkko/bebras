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
    private boolean editable;

    public ProblemWidget(Problem problem) {
        problemType = problem.getType();
        editable = problem.editable();
    }

    @Override
    public List<ResourceLink> links() {
        ResourceLink js = new ResourceLink(problemType + ".problem", "js");
        ResourceLink css = new ResourceLink(problemType + ".problem", "css");

        if (editable)
            return Arrays.asList(new ResourceLink(problemType + ".edit.problem", "js"), js, css);
        else
            return Arrays.asList(js, css);
    }
}
