package models.newproblems.bebras;

import models.utils.Utils;
import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import play.api.templates.Html;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 23:17
 */
public class BebrasProblem implements Problem {

    public static Map<String, String> COUNTRY_TO_NAME = Utils.linkedMapify(
            "_MARS", "Марс",
            "AT", "Австрия",
            "BE", "Бельгия",
            "BG", "Болгария",
            "CA", "Канада",
            "CH", "Швейцария",
            "CZ", "Чехия",
            "DE", "Германия",
            "EE", "Эстония",
            "ES", "Испания",
            "FI", "Финляндия",
            "FR", "Франция",
            "HU", "Венгрия",
            "IT", "Италия",
            "JP", "Япония",
            "LV", "Латвия",
            "LT", "Литва",
            "NL", "Нидерланды",
            "PL", "Польша",
            "RU", "Россия",
            "SE", "Швеция",
            "SI", "Словения",
            "SK", "Словакия",
            "TW", "Тайвань",
            "UA", "Украина",
            "US", "Америка"
    );

    private String title;
    private String country; /*two letters country code according to ISO*/
    private String statement;
    private String question;
    private List<String> answers;
    private int answersLayout; //layout of answers, number of lines with answers:1,2,3,5
    private int rightAnswer; //1, 2, 3, 4
    private String explanation;
    private String informatics;

    public BebrasProblem() {
    }

    public BebrasProblem(String title, String country, String statement, String question, List<String> answers, int answersLayout, int rightAnswer, String explanation, String informatics) {
        this.title = title;
        this.country = country;
        this.statement = statement;
        this.question = question;
        this.answers = answers;
        this.answersLayout = answersLayout;
        this.rightAnswer = rightAnswer;
        this.explanation = explanation;
        this.informatics = informatics;
    }

    @Override
    public Html format(int index, boolean showSolutions, Info settings) {
        //render answers
        Html answersHtml;
        switch (answersLayout) {
            case 1:
                answersHtml = views.html.bebras.answers_1x5.render(answers);
                break;
            case 2:
                answersHtml = views.html.bebras.answers_2x3.render(answers);
                break;
            case 3:
                answersHtml = views.html.bebras.answers_3x2.render(answers);
                break;
            case 5:
                answersHtml = views.html.bebras.answers_5x1.render(answers);
                break;
            default:
                answersHtml = views.html.bebras.answers_5x1.render(answers);
        }

        int scores = 0;
        if (settings != null) {
            Object oScores = settings.get("r");
            if (oScores != null && oScores instanceof Integer)
                scores = (Integer) oScores;
        }

        return views.html.bebras.bebras_problem.render(index, scores, showSolutions, title, country, COUNTRY_TO_NAME.get(country), statement, question, answersHtml, rightAnswer, explanation, informatics);
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public Html formatEditor() {
        return views.html.bebras.bebras_editor.render(title, country, statement, question, answers, rightAnswer, answersLayout, explanation, informatics);
    }

    @Override
    public void updateProblem(RawForm form) {
        title = form.get("title");
        country = form.get("country");
        statement = form.get("statement");
        question = form.get("question");

        answers = new ArrayList<>();
        answers.add(form.get("answers-0"));
        answers.add(form.get("answers-1"));
        answers.add(form.get("answers-2"));
        answers.add(form.get("answers-3"));

        try {
            rightAnswer = Integer.parseInt(form.get("rightAnswer"));
            answersLayout = Integer.parseInt(form.get("answersLayout"));
        } catch (NumberFormatException ignored) {
        }

        explanation = form.get("explanation");
        informatics = form.get("informatics");
    }

    @Override
    public String answerToString(Info answer) {
        if (answer == null)
            return "-";

        Integer ansInt = (Integer) answer.get("a");
        if (ansInt < 0)
            return ".";

        Info check = check(answer);
        int res = (Integer) check.get("result");

        //далее русские А
        if (res < 0)
            return (char)(ansInt + 'а') + "";
        else if (res == 0)
            return ".";
        else
            return (char)(ansInt + 'А') + "";
    }

    @Override
    public Info check(Info answer) {
        Info result = new Info();

        Integer ans = (Integer) answer.get("a");

        if (ans == null) {
            result.put("result", 0);
            result.put("answer", ".");
        } else if (ans < 0) {
            result.put("result", 0);
            result.put("answer", ".");
        } else {
            result.put("result", ans == rightAnswer ? 1 : -1);
            result.put("answer", numberAnswer2string(ans));
        }

        return result;
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return new InfoPattern("a", new BasicSerializationType<>(int.class), "answer");
    }

    @Override
    public String getType() {
        return "bebras";
    }

    @Override
    public Widget getWidget(boolean editor) {
        if (editor)
            return new ListWidget(
                    new ResourceLink("bebras.problem.css"),
                    new ResourceLink("bebras.edit.problem.js"),
                    new ResourceLink("bebras.problem.js")
            );
        else
            return new ListWidget(
                    new ResourceLink("bebras.problem.css"),
                    new ResourceLink("bebras.problem.js")
            );
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
        serializer.write("country", country);
        serializer.write("statement", statement);
        serializer.write("question", question);
        SerializationTypesRegistry.list(String.class).write(serializer, "answers", answers);
        serializer.write("answers layout", answersLayout);
        serializer.write("right", rightAnswer);
        serializer.write("explanation", explanation);
        serializer.write("informatics", informatics);
    }

    @Override
    public void update(Deserializer deserializer) {
        title = deserializer.readString("title", "");
        country = deserializer.readString("country", "").toUpperCase();
        statement = deserializer.readString("statement", "");
        question = deserializer.readString("question", "");
        answers = SerializationTypesRegistry.list(String.class).read(deserializer, "answers");
        answersLayout = deserializer.readInt("answers layout", 0);
        rightAnswer = deserializer.readInt("right", 0);
        explanation = deserializer.readString("explanation", "");
        informatics = deserializer.readString("informatics", "");

        while (answers.size() < 4)
            answers.add("");
    }

    public static String numberAnswer2string(int answer) {
        if (answer < 0)
            return "";
        return String.valueOf((char)('А' + answer)); //русское А
    }
}
