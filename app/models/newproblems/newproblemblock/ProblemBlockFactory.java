package models.newproblems.newproblemblock;

import models.Contest;
import models.newproblems.ProblemLink;
import models.newserialization.Deserializer;
import models.results.Info;
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

    private static final Pattern ONE_PROBLEM = Pattern.compile("problem (.*)");                            //problem /PROB
    private static final Pattern FOLDER = Pattern.compile("folder (.*)");                                  //folder  /FOLDER
    private static final Pattern RANDOM_FOLDER = Pattern.compile("(\\d+) (first )?random <- (.*)");        //2 random <- /FOLDER
    private static final Pattern RANDOM = Pattern.compile("(\\d+) random from (.*?)( in (.*))?");          //5 random from [] [] [] [] (in [])
    private static final Pattern DIRECT = Pattern.compile("problems (.*?)( in (.*))?");                    //problems [] [] [] [] (in [])

    public static ProblemBlock getBlock(Contest contest, String configuration, Info translatorConfiguration) {
        Matcher matcher;

        matcher = ONE_PROBLEM.matcher(configuration);
        if (matcher.matches())
            return oneProblem(contest, matcher.group(1), translatorConfiguration);

        matcher = FOLDER.matcher(configuration);
        if (matcher.matches())
            return folder(contest, matcher.group(1), translatorConfiguration);

        matcher = RANDOM_FOLDER.matcher(configuration);
        if (matcher.matches())
            return randomFolder(contest, Integer.parseInt(matcher.group(1)), matcher.group(2), matcher.group(3), translatorConfiguration);

        matcher = RANDOM.matcher(configuration);
        if (matcher.matches())
            return random(contest, Integer.parseInt(matcher.group(1)), matcher.group(2), matcher.group(4), translatorConfiguration);

        matcher = DIRECT.matcher(configuration);
        if (matcher.matches())
            return direct(contest, matcher.group(1), matcher.group(3), translatorConfiguration);

        return null;
    }

    public static ProblemBlock getBlock(Contest contest, Deserializer deserializer) {
        String type = deserializer.readString("type");

        switch (type) {
            case "random":
                return new RandomProblemBlock(contest, deserializer);
            case "direct":
                return new DirectProblemBlock(contest, deserializer);
        }

        throw new IllegalArgumentException("Unknown type for problem block: " + type);
    }

    private static ProblemBlock oneProblem(Contest contest, String link, Info translatorConfiguration) {
        link = absoluteLink(contest, link);

        ProblemLink problemLink = new ProblemLink(link);

        ObjectId pid = problemLink.getProblemId();

        contest.registerProblemName(pid, problemLink.getName());

        return new DirectProblemBlock(contest, pid, translatorConfiguration);
    }

    private static ProblemBlock folder(Contest contest, String link, Info translatorConfiguration) {
        link = absoluteLink(contest, link);

        ProblemLink problemLink = new ProblemLink(link);

        List<ProblemLink> list = problemLink.listProblems();

        List<ObjectId> pids = new ArrayList<>();

        for (ProblemLink pl : list) {
            ObjectId pid = pl.getProblemId();
            contest.registerProblemName(pid, pl.getName());
            pids.add(pid);
        }

        return new DirectProblemBlock(contest, pids, translatorConfiguration);
    }

    private static ProblemBlock random(Contest contest, int count, String problemsList, String folder, Info translatorConfiguration) {
        if (folder == null)
            folder = contestFolder(contest);
        else
            folder = absoluteLink(contest, folder);

        List<ObjectId> pids = new ArrayList<>();
        String[] problems = problemsList.split(" ");

        for (String problem : problems) {
            ProblemLink pl = new ProblemLink(absoluteLink(folder, problem.trim()));
            ObjectId pid = pl.getProblemId();

            if (pid == null)
                continue;

            contest.registerProblemName(pid, pl.getName());
            pids.add(pid);
        }

        return new RandomProblemBlock(contest, count, false, pids, translatorConfiguration);
    }

    private static ProblemBlock direct(Contest contest, String problemsList, String folder, Info translatorConfiguration) {
        if (folder == null)
            folder = contestFolder(contest);
        else
            folder = absoluteLink(contest, folder);

        List<ObjectId> pids = new ArrayList<>();
        String[] problems = problemsList.split(" ");

        for (String problem : problems) {
            ProblemLink pl = new ProblemLink(absoluteLink(folder, problem.trim()));
            ObjectId pid = pl.getProblemId();

            if (pid == null)
                continue;

            contest.registerProblemName(pid, pl.getName());
            pids.add(pid);
        }

        return new DirectProblemBlock(contest, pids, translatorConfiguration);
    }

    private static ProblemBlock randomFolder(Contest contest, int count, String takeOnlyFirst, String link, Info translatorConfiguration) {
        link = absoluteLink(contest, link);

        ProblemLink problemLink = new ProblemLink(link);

        List<ProblemLink> list = problemLink.listProblems();

        List<ObjectId> pids = new ArrayList<>();

        for (ProblemLink pl : list) {
            ObjectId pid = pl.getProblemId();
            contest.registerProblemName(pid, pl.getName());
            pids.add(pid);
        }

        return new RandomProblemBlock(contest, count, takeOnlyFirst != null, pids, translatorConfiguration);
    }

    private static String contestFolder(Contest contest) {
        return contest.getEvent().getId() + '/' + contest.getId();
    }

    private static String absoluteLink(Contest contest, String link) {
        return absoluteLink(contestFolder(contest), link);
    }

    private static String absoluteLink(String base, String link) {
        if (link.startsWith("/"))
            link = link.substring(1);
        else
            link = base + '/' + link;
        return link;
    }

}
