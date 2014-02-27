package models.newproblems.kio;

import models.User;
import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.JSONDeserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.api.templates.Html;
import play.mvc.Http;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.io.File;
import java.io.IOException;

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
        return new InfoPattern();
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

    public boolean processFile(File solutionFile) {
        //Http.Context.current().flash().put()
        User user = User.current();
        Http.Flash flash = Http.Context.current().flash();

        // try read file contents
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jfactory = mapper.getJsonFactory();
        try (JsonParser jParser = jfactory.createJsonParser(solutionFile)) {
            JsonNode jsonNode = jParser.readValueAsTree();

            JsonNode kioBase = jsonNode.get("kio_base");

            if (kioBase == null)
                throw new Exception();

            JsonNode anketa = kioBase.get("anketa");

            if (anketa == null)
                throw new Exception();

            JsonNode name = anketa.get("name");
            JsonNode surname = anketa.get("name");

            if (name == null || surname == null)
                throw new Exception();

            String anketaName = name.asText().trim().toLowerCase();
            String anketaSurname = surname.asText().trim().toLowerCase();

            String userName = (String) user.getInfo().get("name"); //NullPointerException may be only when hacked, because normal user must have name and surname
            String userSurname = (String) user.getInfo().get("surname");

            String normalizedUserName = userName.trim().toLowerCase();
            String normalizedUserSurname = userSurname.trim().toLowerCase();

            if (!normalizedUserName.equals(anketaName) && !normalizedUserSurname.equals(anketaSurname)) {
                flash.put(MESSAGE_KEY, "В анкете в загруженном файле указан участник: " + name.asText() + " " + surname.asText() + ", " +
                        "но ваше имя: " + userName + " " + userSurname + ". " +
                        "Вы можете исправить своё имя на сайте в разделе \"личные данные\", либо в анкете в программе конкурса.");
                return false;
            }

        } catch (Exception e) {
            flash.put(MESSAGE_KEY, "Не удалось прочитать файл с решением. Убедитесь, что вы посылаете правильный файл или попробуйте еще раз.");
            Logger.error("Error while parsing solution file", e);
            return false;
        }

        return true;
    }
}
