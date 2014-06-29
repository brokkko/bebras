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

    public RandomProblemBlock(Contest contest, Deserializer deserializer) {
        super(contest, deserializer);

        count = deserializer.readInt("cnt");
        takeOnlyFirst = deserializer.readBoolean("tof", false);
        pids = SerializationTypesRegistry.list(ObjectId.class).read(deserializer, "pids");
    }

    public RandomProblemBlock(Contest contest, Info configuration) {
        super(contest, configuration);
    }

    public RandomProblemBlock(Contest contest, int count, boolean takeOnlyFirst, List<ObjectId> pids, Info configuration) {
        super(contest, configuration);
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

        int cnt = Math.min(count, problems.size());

        if (takeOnlyFirst) {
            problems = problems.subList(0, cnt);

            Random random = new Random(user.getContestRandSeed(contest.getId()));
            Collections.shuffle(problems, random);
        } else {
            int hash = 0;
            for (ObjectId pid : pids)
                hash += pid.hashCode();

            Random random = new Random(user.getContestRandSeed(contest.getId()) + hash); //does not really matters what to hash
            Collections.shuffle(problems, random);

            problems = problems.subList(0, cnt);
        }

        return problems;
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
            result.add(new ConfiguredProblem(pid, info.getProblem(), contest.getProblemName(pid), getConfiguration()));
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

        serializer.write("type", "random");
        serializer.write("cnt", count);
        serializer.write("tof", takeOnlyFirst);
        SerializationTypesRegistry.list(ObjectId.class).write(serializer, "pids", pids);
    }

    @Override
    public void substituteIds(Map<ObjectId, ObjectId> problemOld2New) {
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
