package models.newproblems.newproblemblock;

import models.Contest;
import models.newproblems.ProblemLink;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.07.13
 * Time: 18:35
 */
public class ProblemBlockFactory {

    private static final Pattern ONE_PROBLEM = Pattern.compile("problem /(.*)");
    private static final Pattern FOLDER = Pattern.compile("folder /(.*)");
    private static final Pattern RANDOM = Pattern.compile("(\\d+) (first )?random <- /(.*)");

    public static ProblemBlock getBlock(Contest contest, String configuration) {
        Matcher matcher;

        matcher = ONE_PROBLEM.matcher(configuration);
        if (matcher.matches())
            return oneProblem(contest, matcher.group(1));

        matcher = FOLDER.matcher(configuration);
        if (matcher.matches())
            return folder(contest, matcher.group(1));

        matcher = RANDOM.matcher(configuration);
        if (matcher.matches())
            return random(contest, Integer.parseInt(matcher.group(1)), matcher.group(2), matcher.group(3));

        return null;
    }

    private static ProblemBlock oneProblem(Contest contest, String link) {
        ProblemLink problemLink = new ProblemLink(link);

        ObjectId pid = problemLink.getProblemId();

        contest.registerProblemName(pid, problemLink.getName());

        return new DirectProblemBlock(pid);
    }

    private static ProblemBlock folder(Contest contest, String link) {
        ProblemLink problemLink = new ProblemLink(link);

        List<ProblemLink> list = problemLink.list();

        List<ObjectId> pids = new ArrayList<>();

        for (ProblemLink pl : list) {
            ObjectId pid = pl.getProblemId();
            contest.registerProblemName(pid, pl.getName());
            pids.add(pid);
        }

        return new DirectProblemBlock(pids);
    }

    private static ProblemBlock random(Contest contest, int count, String takeOnlyFirst, String link) {
        //warning. code duplication with method folder()
        ProblemLink problemLink = new ProblemLink(link);

        List<ProblemLink> list = problemLink.list();

        List<ObjectId> pids = new ArrayList<>();

        for (ProblemLink pl : list) {
            ObjectId pid = pl.getProblemId();
            contest.registerProblemName(pid, pl.getName());
            pids.add(pid);
        }

        return new RandomProblemBlock(count, takeOnlyFirst != null, pids);
    }

}
