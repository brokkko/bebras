package models.problems.problemblock;

import models.Contest;
import models.User;
import models.problems.ConfiguredProblem;
import models.problems.LinkProblem;
import models.problems.Problem;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.05.13
 * Time: 18:10
 */
public class OneProblemBlock extends ProblemBlock {

    private String link;
    private Problem problem;

    public OneProblemBlock(Contest contest) {
        super(contest);
    }

    @Override
    public List<ConfiguredProblem> getProblems(User user) {
        return Arrays.asList(new ConfiguredProblem(link, problem));
    }

    @Override
    protected Pattern getConfigurationPattern() {
        return Pattern.compile("problem /(.*)");
    }

    @Override
    protected void configure(Matcher matcher) {
        link = matcher.group(1);
        problem = new LinkProblem(link);
    }
}
