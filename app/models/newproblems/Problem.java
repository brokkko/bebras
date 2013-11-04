package models.newproblems;

import models.forms.RawForm;
import models.newserialization.SerializableUpdatable;
import models.results.Info;
import models.results.InfoPattern;
import play.api.templates.Html;
import views.widgets.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.07.13
 * Time: 13:15
 */
public interface Problem extends SerializableUpdatable {

    Html format(int index, boolean showSolutions, Info settings);

    boolean editable();

    Html formatEditor(); //TODO make it a usual form

    void updateProblem(RawForm form); //TODO make it a usual form

    String answerToString(Info answer); //TODO check result also may be needed

    Info check(Info answer);

    InfoPattern getAnswerPattern();

    String getType();

    Widget getWidget(boolean editor);
}