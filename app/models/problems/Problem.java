package models.problems;

import models.serialization.Deserializer;
import models.serialization.Serializer;
import play.api.templates.Html;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 11:09
 */
public interface Problem {

    Html formatStatement();

    Html formatStatementWithSolution();

    String getJsLink();

    String getCssLink();

    void check(Deserializer submission, Serializer resultsReceiver);

}
