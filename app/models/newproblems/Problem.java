package models.newproblems;

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

    String answerToString(Info answer); //TODO check result also may be needed

    Info check(Info answer);

    InfoPattern getAnswerPattern();

    String getType();
}