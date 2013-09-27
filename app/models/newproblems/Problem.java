package models.newproblems;

import models.forms.RawForm;
import models.newserialization.SerializableUpdatable;
import models.results.Info;
import models.results.InfoPattern;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.07.13
 * Time: 13:15
 */
public interface Problem extends SerializableUpdatable {

    Html format(int index, boolean showSolutions);

    boolean editable();

    Html formatEditor(); //TODO make it a usual form

    void updateProblem(RawForm form); //TODO make it a usual form

    String answerToString(Info answer); //TODO check result also may be needed

    Info check(Info answer);

    InfoPattern getAnswerPattern();

    String getType();
}