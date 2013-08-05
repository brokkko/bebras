package models.newproblems.newproblemblock;

import models.Contest;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.ProblemInfo;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.07.13
 * Time: 18:39
 */
public class RandomProblemBlock extends ProblemBlock {

    private int count;
    private boolean takeOnlyFirst;
    private List<ObjectId> pids;

    public RandomProblemBlock() {
    }

    public RandomProblemBlock(int count, boolean takeOnlyFirst, List<ObjectId> pids) {
        this.count = count;
        this.takeOnlyFirst = takeOnlyFirst;
        this.pids = pids;
    }

    @Override
    public String getInfoString() {
        String info = count + " random problems from";
        if (takeOnlyFirst)
            info += " (only first)";
        return info;
    }

    @Override
    public List<ConfiguredProblem> getProblems(Contest contest, User user) {
        List<ConfiguredProblem> problems = getAllPossibleProblems(contest);

        if (takeOnlyFirst) {
            problems = problems.subList(0, count);

            Random random = new Random(user.getContestRandSeed(contest.getId()));
            Collections.shuffle(problems, random);
        } else {
            int hash = 0;
            for (ObjectId pid : pids)
                hash += pid.hashCode();

            Random random = new Random(user.getContestRandSeed(contest.getId()) + hash); //does not really matters what to hash
            Collections.shuffle(problems, random);

            problems = problems.subList(0, count);
        }

        return problems;
    }

    @Override
    public List<ConfiguredProblem> getAllPossibleProblems(Contest contest) {
        int n = getProblemsCount();
        List<ConfiguredProblem> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ProblemInfo info = ProblemInfo.get(pids.get(i));
            ObjectId pid = info.getId();
            result.add(new ConfiguredProblem(pid, info.getProblem(), contest.getProblemName(pid), null));
        }

        return result;
    }

    @Override
    public int getProblemsCount() {
        return pids.size();
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("cnt", count);
        serializer.write("tof", takeOnlyFirst);
        SerializationTypesRegistry.list(ObjectId.class).write(serializer, "pids", pids);
    }

    @Override
    public void update(Deserializer deserializer) {
        count = deserializer.readInt("cnt");
        takeOnlyFirst = deserializer.readBoolean("tof", false);
        pids = SerializationTypesRegistry.list(ObjectId.class).read(deserializer, "pids");
    }
}
