package models.newproblems.newproblemblock;

import models.Contest;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.ProblemInfo;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import models.results.Info;
import org.bson.types.ObjectId;
import play.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.07.13
 * Time: 18:39
 */
public class DirectProblemBlock extends ProblemBlock {

    private List<ObjectId> pids;

    public DirectProblemBlock(Contest contest, Deserializer deserializer) {
        super(contest, deserializer);
        pids = SerializationTypesRegistry.list(ObjectId.class).read(deserializer, "pids");
    }

    public DirectProblemBlock(Contest contest, Info configuration) {
        super(contest, configuration);
    }

    public DirectProblemBlock(Contest contest, List<ObjectId> pids, Info configuration) {
        super(contest, configuration);
        this.pids = pids;
    }

    public DirectProblemBlock(Contest contest, ObjectId problemId, Info configuration) {
        super(contest, configuration);
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

            if (info == null) {
                Logger.error("Problem Block with null pid");
                continue;
            }

            ObjectId pid = info.getId();
            result.add(new ConfiguredProblem(pid, info.getProblem(), contest.getProblemName(pid), getConfiguration())); //TODO now settings are null, load settings from deserializer, take infopattern from
        }

        return result;
    }

    @Override
    public int getProblemsCount() {
        return pids.size();
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("type", "direct");
        SerializationTypesRegistry.list(ObjectId.class).write(serializer, "pids", pids);
    }

    @Override
    public void substituteIds(Map<ObjectId, ObjectId> problemOld2New) {
        //TODO this code is the same as in RandomProblemBlock
        List<ObjectId> newPids = new ArrayList<>(pids.size());

        for (ObjectId pid : pids) {
            ObjectId newPid = problemOld2New.get(pid);
            if (newPid == null) {
                Logger.warn("strange problem pid");
                continue;
            }
            newPids.add(newPid);
        }

        pids = newPids;
    }
}
