package models.problems;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 14:09
 */
public class MountProblemSource extends ProblemSource {

    protected final Map<String, ProblemSource> sources = new HashMap<>();
    protected final Map<String, Problem> problems = new HashMap<>();

    @Override
    public void mount(String root, ProblemSource source) {
        sources.put(root, source);
    }

    @Override
    public void put(String id, Problem problem) {
        problems.put(id, problem);
    }

    @Override
    protected Problem getDirectProblem(String id) {
        return problems.get(id);
    }

    @Override
    public List<String> list() {
        //todo report: not highlighted return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
        return new ArrayList<>(problems.keySet());
    }

    @Override
    public List<String> listSubsources() {
        return new ArrayList<>(sources.keySet());
    }

    @Override
    protected ProblemSource getDirectSubsource(String folder) {
        return sources.get(folder);
    }
}
