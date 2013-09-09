package models.newproblems.bebras;

import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 23:17
 */
public class BebrasProblem implements Problem {
    @Override
    public Html format(int index, boolean showSolutions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Html formatEditor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateProblem(RawForm form) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String answerToString(Info answer) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Info check(Info answer) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void serialize(Serializer serializer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update(Deserializer deserializer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
