package models.problems.problemblock;

import models.problems.ConfiguredProblem;
import models.problems.LinkProblemSource;
import models.problems.Problem;
import models.problems.ProblemSource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.05.13
 * Time: 18:17
 */
public class FolderBlock extends ProblemBlock {

    private String link;
    private ProblemSource problemSource;

    @Override
    public List<ConfiguredProblem> getProblems(String userId) {
        List<String> list = problemSource.list();
        List<ConfiguredProblem> problems = new ArrayList<>(list.size());

        for (String id : list)
            problems.add(new ConfiguredProblem(link + '/' + id, problemSource.get(id)));

        return problems;
    }

    @Override
    protected Pattern getConfigurationPattern() {
        return Pattern.compile("folder /(.*)");
    }

    @Override
    protected void configure(Matcher matcher) {
        link = matcher.group(1);
        problemSource = new LinkProblemSource(link);
    }
}
