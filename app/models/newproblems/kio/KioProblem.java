package models.newproblems.kio;

import models.User;
import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import org.bson.types.ObjectId;
import play.api.templates.Html;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 23:17
 */
public class KioProblem implements Problem {

    private ObjectId kioId = new ObjectId(); //TODO get rid of this. The id is needed only to find a problem on submit

    public static String MESSAGE_KEY = "kio_problem_message";

    private static InfoPattern KIO14_ANSWER_PATTERN = new InfoPattern(
            "_level", new BasicSerializationType<>(int.class), "уровень",

            "stars",
            new InfoPattern(
                    "has_intersected_lines", new BasicSerializationType<>(boolean.class), "Пересекающиеся линии",
                    "total_number_of_difference_graphs", new BasicSerializationType<>(int.class), "Различных созвездий",
                    "total_number_of_right_graphs", new BasicSerializationType<>(int.class), "Всего созвездий",
                    "sum_of_lines", new BasicSerializationType<>(double.class), "Длина линий"
            ),
            "Создвездия",

            "fountains",
            new InfoPattern(
                  "total_length", new BasicSerializationType<>(int.class), "Длина струй"
            ),
            "Фонтаны",

            "jacks_house",
            new InfoPattern(
                    "statements", new BasicSerializationType<>(int.class), "Выполнено утверждений",
                    "figures", new BasicSerializationType<>(int.class), "Фигурок"
            ),
            "Создвездия",

            "tarski",
            new InfoPattern(
                    "statements", new BasicSerializationType<>(int.class), "Выполнено утверждений",
                    "length", new BasicSerializationType<>(int.class), "Условий"
            ),
            "Вилларибо и Виллабаджо"
    );

    public KioProblem() {
    }

    @Override
    public Html format(String index, boolean showSolutions, Info settings, long randSeed) {
        User user = User.current();

        return views.html.kio.kio_problem.render(kioId);
    }

    @Override
    public boolean editable() {
        return false; //TODO make it editable
    }

    @Override
    public Html formatEditor() {
        return null;
    }

    @Override
    public void updateProblem(RawForm form) {
    }

    @Override
    public String answerToString(Info answer, long randSeed) {
        return ""; //TODO display answer as a string
    }

    @Override
    public String answerString() {
        return "";
    }

    @Override
    public Info check(Info answer, long randSeed) {
        return answer;
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return KIO14_ANSWER_PATTERN;
    }

    @Override
    public String getType() {
        return "kio";
    }

    @Override
    public Widget getWidget(boolean editor) {
        return new ListWidget(
                new ResourceLink("kio.problem.css"),
                new ResourceLink("kio.problem.js")
        );
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("kio_id", kioId);
    }

    @Override
    public void update(Deserializer deserializer) {
        kioId = deserializer.readObjectId("kio_id", new ObjectId());
    }

    public ObjectId getKioId() {
        return kioId;
    }

    public void processFile(File solutionFile) {
        //Http.Context.current().flash().put()
        User user = User.current();
    }
}
