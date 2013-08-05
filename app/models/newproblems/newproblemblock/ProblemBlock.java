package models.newproblems.newproblemblock;

import models.Contest;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.ProblemInfo;
import models.newserialization.SerializableUpdatable;
import play.api.templates.Html;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.07.13
 * Time: 18:32
 */
public abstract class ProblemBlock implements SerializableUpdatable {

    public abstract String getInfoString();

    public abstract List<ConfiguredProblem> getProblems(Contest contest, User user);

    public abstract List<ConfiguredProblem> getAllPossibleProblems(Contest contest);

    public abstract int getProblemsCount();

}
