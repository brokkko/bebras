package models.problems;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 13:59
 */
public abstract class ProblemSource {

    //TODO report a bug that if interface method is called get, then it is not detected as not used

    public void mount(String root, ProblemSource source) {
        throw new UnsupportedOperationException();
    }

    public void put(String id, Problem problem) {
        throw new UnsupportedOperationException();
    }

    public abstract List<String> list();

    public abstract List<String> listSubsources();

    public Problem get(String id) {
        int slashPos = id.indexOf('/');
        if (slashPos < 0)
            return getDirectProblem(id);
        else
            return getDirectSubsource(id.substring(0, slashPos)).get(id.substring(slashPos + 1));
    }

    public ProblemSource getSubsource(String id) {
        int slashPos = id.indexOf('/');
        if (slashPos < 0)
            return getDirectSubsource(id);
        else
            return getDirectSubsource(id.substring(0, slashPos)).getSubsource(id.substring(slashPos + 1));
    }

    public ProblemSource getSubsourceOrCreate(String folder) {
        String[] folders = folder.split("///g");
        ProblemSource currentSource = this;
        for (String f : folders) {
            ProblemSource nextSource = getSubsource(f);
            if (nextSource == null) {
                nextSource = new MountProblemSource();
                currentSource.mount(f, nextSource);
            }
            currentSource = nextSource;
        }

        return currentSource;
    }

    protected abstract ProblemSource getDirectSubsource(String folder);

    protected abstract Problem getDirectProblem(String id);
}