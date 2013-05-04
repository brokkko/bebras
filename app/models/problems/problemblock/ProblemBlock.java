package models.problems.problemblock;

import models.problems.ConfiguredProblem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.05.13
 * Time: 16:48
 */
public abstract class ProblemBlock { //TODO implement Serializable

    public boolean acceptsConfiguration(String configuration) {
        if (configuration == null)
            return false;

        Matcher matcher = getConfigurationPattern().matcher(configuration);
        boolean matches = matcher.matches();
        if (matches)
            configure(matcher);

        return matches;
    }

    public abstract List<ConfiguredProblem> getProblems(String userId);

    protected abstract Pattern getConfigurationPattern();

    protected abstract void configure(Matcher matcher);
}
