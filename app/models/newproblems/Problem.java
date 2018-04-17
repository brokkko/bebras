package models.newproblems;

import models.forms.RawForm;
import models.newserialization.SerializableUpdatable;
import models.results.Info;
import models.results.InfoPattern;
import play.twirl.api.Html;
import views.widgets.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.07.13
 * Time: 13:15
 */
public interface Problem extends SerializableUpdatable {

    //TODO what are these settings?
    Html format(String index, boolean showSolutions, Info settings, long seed);

    boolean editable();

    Html formatEditor(); //TODO make it a usual form

    void updateProblem(RawForm form); //TODO make it a usual form

    /**
     * Convert user answer to a string representation for history
     * @param answer answer
     * @param randSeed random seed
     * @return string representation of an answer
     */
    String answerToString(Info answer, long randSeed); //TODO check result may also be needed

    /**
     * correct answer
     * @return string representation of a correct answer,
     */
    String answerString();

    /**
     * Converts user answer to a contest results, should be consistent with the used results translator
     * @param answer user answer
     * @param randSeed random seed
     * @return answer converted to a result
     */
    Info check(Info answer, long randSeed);

    /**
     * Answer pattern used to store answers in DB
     * @return
     */
    InfoPattern getAnswerPattern();

    InfoPattern getCheckerPattern();

    String getType();

    Widget getWidget(boolean editor);
}