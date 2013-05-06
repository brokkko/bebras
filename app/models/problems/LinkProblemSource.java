package models.problems;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.05.13
 * Time: 14:00
 */
public class LinkProblemSource extends ProblemSource {

    private final String link;

    public LinkProblemSource(String link) {
        this.link = link;
    }

    @Override
    public List<String> list() {
        return loadSource().list();
    }

    @Override
    public List<String> listSubsources() {
        return loadSource().listSubsources();
    }

    @Override
    protected ProblemSource getDirectSubsource(String folder) {
        return loadSource().getDirectSubsource(link);
    }

    @Override
    protected Problem getDirectProblem(String id) {
        return loadSource().getDirectProblem(id);
    }

    private ProblemSource loadSource() {
        return RootProblemSource.getInstance().getSubsource(link);
    }
}
