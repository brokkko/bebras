package models.newproblems.newproblemblock;

import models.Contest;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.ProblemInfo;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.07.13
 * Time: 18:39
 */
public class DirectProblemBlock extends ProblemBlock {

    private List<ObjectId> pids;

    public DirectProblemBlock() {
    }

    public DirectProblemBlock(List<ObjectId> pids) {
        this.pids = pids;
    }

    public DirectProblemBlock(ObjectId problemId) {
        pids = new ArrayList<>();
        pids.add(problemId);
    }

    @Override
    public String getInfoString() {
        return "All problems from list";
    }

    @Override
    public List<ConfiguredProblem> getProblems(Contest contest, User user) {
        return getAllPossibleProblems(contest);
    }

    @Override
    public List<ConfiguredProblem> getAllPossibleProblems(Contest contest) {
        int n = getProblemsCount();
        List<ConfiguredProblem> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ProblemInfo info = ProblemInfo.get(pids.get(i));

            if (info == null)
                continue;

            ObjectId pid = info.getId();
            result.add(new ConfiguredProblem(pid, info.getProblem(), contest.getProblemName(pid), null)); //TODO now settings are null, load settings from deserializer, take infopattern from
        }

        return result;
    }

    @Override
    public int getProblemsCount() {
        return pids.size();
    }

    @Override
    public void serialize(Serializer serializer) {
        SerializationTypesRegistry.list(ObjectId.class).write(serializer, "pids", pids);
    }

    @Override
    public void update(Deserializer deserializer) {
        pids = SerializationTypesRegistry.list(ObjectId.class).read(deserializer, "pids");
    }
}
