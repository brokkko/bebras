package models.problems.problemblock;

import models.problems.LinkProblemSource;
import models.problems.Problem;
import models.problems.ProblemSource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.05.13
 * Time: 18:43
 */
public class RandomProblemsBlock extends ProblemBlock {

    private int count;
    private ProblemSource problemSource;

    @Override
    public List<Problem> getProblems(String userId) {
        List<String> list = problemSource.list();
        List<Problem> problems = new ArrayList<>(list.size());

        for (String id : list)
            problems.add(problemSource.get(id));

        problems = problems.subList(0, count);

        Random random = new Random(userId.hashCode());
        Collections.shuffle(problems, random);

        return problems;
    }

    @Override
    protected Pattern getConfigurationPattern() {
        return Pattern.compile("(\\d+) random <- /(.*)");
    }

    @Override
    protected void configure(Matcher matcher) {
        count = Integer.parseInt(matcher.group(1));
        problemSource = new LinkProblemSource(matcher.group(2));
    }
}
