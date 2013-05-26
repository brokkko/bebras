package models.problems.problemblock;

import models.Contest;
import models.User;
import models.problems.ConfiguredProblem;
import models.problems.LinkProblemSource;
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

    private String link;
    private int count;
    private ProblemSource problemSource;

    public RandomProblemsBlock(Contest contest) {
        super(contest);
    }

    @Override
    public List<ConfiguredProblem> getProblems(User user) {
        List<String> list = problemSource.list();
        List<ConfiguredProblem> problems = new ArrayList<>(list.size());

        for (String id : list)
            problems.add(new ConfiguredProblem(link + '/' + id, problemSource.get(id)));

        problems = problems.subList(0, count);

        Random random = new Random(user.getContestRandSeed(contest.getId()));
//        Random random = new Random(user.getId().hashCode());
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
        link = matcher.group(2);
        problemSource = new LinkProblemSource(link);
    }

    @Override
    public Collection<? extends String> getAllPossibleProblems() {
        List<String> list = problemSource.list();
        List<String> result = new ArrayList<>(list.size());

        for (String s : list)
            result.add(link + '/' + s); //TODO code duplication

        return result.subList(0, count); //TODo this sublist is a hack
    }

    @Override
    public int getProblemsCount() {
        return count;
    }
}
