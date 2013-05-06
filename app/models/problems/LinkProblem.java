package models.problems;

import models.serialization.Deserializer;
import models.serialization.Serializer;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 02.05.13
 * Time: 13:47
 */
public class LinkProblem implements Problem {

    private final String link;

    public LinkProblem(String link) {
        this.link = link;
    }

    @Override
    public Html format(int index, boolean showSolutions) {
        return loadProblem().format(index, showSolutions);
    }

    @Override
    public String getJsLink() {
        return loadProblem().getJsLink();
    }

    @Override
    public String getCssLink() {
        return loadProblem().getCssLink();
    }

    @Override
    public void check(Answer answer, Serializer resultsReceiver) {
        loadProblem().check(answer, resultsReceiver);
    }

    @Override
    public String getType() {
        return loadProblem().getType();
    }

    private Problem loadProblem() {
        return RootProblemSource.getInstance().get(link);
    }
}
