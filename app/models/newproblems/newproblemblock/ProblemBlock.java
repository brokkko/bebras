package models.newproblems.newproblemblock;

import models.Contest;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.07.13
 * Time: 18:32
 */
public abstract class ProblemBlock {

    private Contest contest;
    private Info configuration;

    public ProblemBlock(Contest contest, Deserializer deserializer) {
        this.contest = contest;
        Deserializer confDeserializer = deserializer.getDeserializer("conf");

        if (confDeserializer == null)
            configuration = new Info();
        else
            configuration = contest.getResultTranslator().getConfigInfoPattern().read(confDeserializer);
    }

    protected ProblemBlock(Contest contest, Info configuration) {
        this.contest = contest;
        this.configuration = configuration;
    }

    public abstract String getInfoString();

    public abstract List<ConfiguredProblem> getProblems(Contest contest, User user);

    public abstract List<ConfiguredProblem> getAllPossibleProblems(Contest contest);

    public abstract int getProblemsCount();

    public Info getConfiguration() {
        return configuration;
    }

    public void serialize(Serializer serializer) {
        contest.getResultTranslator().getConfigInfoPattern().write(configuration, serializer.getSerializer("conf"));
    }

    public abstract void substituteIds(Map<ObjectId, ObjectId> problemOld2New);
}
