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

    Html format(int index, boolean showSolutions);

    //TODO remove js and css links, use type instead
    String getJsLink();

    String getCssLink();

    void check(Answer answer, Serializer resultsReceiver);

    String getType();
}
