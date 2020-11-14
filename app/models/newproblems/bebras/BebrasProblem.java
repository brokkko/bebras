package models.newproblems.bebras;

import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import models.utils.Utils;
import play.Logger;
import play.twirl.api.Html;
import views.html.bebras.answer_cell;
import views.html.bebras.no_answer_cell;
import views.widgets.EmbeddedLink;
import views.widgets.ListWidget;
import views.widgets.ResourceLink;
import views.widgets.Widget;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.09.13
 * Time: 23:17
 */
public class BebrasProblem implements Problem {

    public static Map<String, String> COUNTRY_TO_NAME = Utils.linkedMapify(
            "_MARS", "Марс",
            "AU", "Австралия",
            "AT", "Австрия",
            "AZ", "Азербайджан",
            "BE", "Бельгия",
            "BG", "Болгария",
            "GB", "Великобритания",
            "CA", "Канада",
            "CH", "Швейцария",
            "CN", "Китай",
            "CY", "Кипр",
            "CZ", "Чехия",
            "DE", "Германия",
            "EE", "Эстония",
            "ES", "Испания",
            "FI", "Финляндия",
            "FR", "Франция",
            "GB", "Великобритания",
            "HR", "Хорватия",
            "HU", "Венгрия",
            "ID", "Индонезия",
            "IE", "Ирландия",
            "IL", "Израиль",
            "IN", "Индия",
            "IR", "Иран",
            "IS", "Исландия",
            "IT", "Италия",
            "JP", "Япония",
            "KR", "Корея",
            "LV", "Латвия",
            "LT", "Литва",
            "MK", "Северная Македония",
            "MY", "Малайзия",
            "NL", "Нидерланды",
            "NZ", "Новая Зеландия",
            "PH", "Филиппины",
            "PK", "Пакистан",
            "PL", "Польша",
            "PT", "Португалия",
            "RU", "Россия",
            "RO", "Румыния",
            "RS", "Сербия",
            "SE", "Швеция",
            "SI", "Словения",
            "SK", "Словакия",
            "TH", "Таиланд",
            "TR", "Турция",
            "TW", "Тайвань",
            "UA", "Украина",
            "US", "США",
            "UY", "Уругвай",
            "VN", "Вьетнам",
            "ZA", "ЮАР"
    );

    private String title;
    private String country; /*two letters country code according to ISO*/
    private String statement;
    private String question;
    private List<String> answers;
    private int answersCount = 4;
    private int answersLayout; //layout of answers, number of lines with answers:1,2,3,5
    private int rightAnswer; //0, 1, 2, 3
    private String explanation;
    private String informatics;
    private String extraJS;

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
    public Html format(String index, boolean showSolutions, Info settings, long randSeed) {
        String _statement = fixTables(statement);
        String _question = fixTables(question);
        String _explanation = fixTables(explanation);
        String _informatics = fixTables(informatics);

        if ("certificate only".equals(question)) {
            return views.html.bebras.school_certificate.render(_statement);
        }

        //render answers
        List<String> userAnswers = getUserAnswers(randSeed);
        Html answersHtml = createAnswersHtml(userAnswers);

        int scores = 0;
        if (settings != null) {
            Object oScores = settings.get("r");
            if (oScores instanceof Integer)
                scores = (Integer) oScores;
        }

        boolean showStatementOnly = "statement only".equals(question);

        if (!showStatementOnly)
            return views.html.bebras.bebras_problem.render(
                    index, scores, showSolutions, title, country,
                    COUNTRY_TO_NAME.get(country), _statement, _question,
                    answersHtml, realAnswerToUserAnswer(rightAnswer, randSeed),
                    _explanation, _informatics
            );
        else
            return views.html.bebras.bebras_problem_statement_only.render(index, title, _statement);
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public Html formatEditor() {
        return views.html.bebras.bebras_editor.render(title, country, statement, question, answersCount, answers, rightAnswer, answersLayout, explanation, informatics, extraJS);
    }

    @Override
    public void updateProblem(RawForm form) {
        title = form.get("title");
        country = form.get("country");
        statement = form.get("statement");
        question = form.get("question");
        answersCount = form.getAsInt("answersCount", 4);

        answers = new ArrayList<>();
        for (int i = 0; i < answersCount; i++)
            answers.add(form.get("answers-" + i));

        rightAnswer = form.getAsInt("rightAnswer", 0);
        answersLayout = form.getAsInt("answersLayout", 1);

        explanation = form.get("explanation");
        informatics = form.get("informatics");
        extraJS = form.get("extrajs");
    }

    public static String fixTables(String text) {
        int cnt = 0;
        Pattern tbl = Pattern.compile("<table");
        Pattern tblEnd = Pattern.compile("</table");

        Matcher m1 = tbl.matcher(text);
        while (m1.find())
            cnt++;
        Matcher m2 = tblEnd.matcher(text);
        while (m2.find())
            cnt--;

        for (int i = 0; i < cnt; i++)
            text += "</table>";

        return text;
    }

    @Override
    public String answerToString(Info answer, long randSeed) {
        if (answer == null)
            return "-";

        Integer ansInt = (Integer) answer.get("a");

        if (ansInt == null) {
            Logger.info("? found");
            return "?";
        }

        if (ansInt < 0)
            return ".";

        ansInt = userAnswerToRealAnswer(ansInt, randSeed);

        Info check = check(answer, randSeed);
        int res = (Integer) check.get("result");

        //далее русские А
        if (res < 0)
            return (char) (ansInt + 'а') + "";
        else if (res == 0)
            return ".";
        else
            return (char) (ansInt + 'А') + "";
    }

    @Override
    public String answerString() {
        return (char) (rightAnswer + 'А') + "";
    }

    @Override
    public Info check(Info answer, long randSeed) {
        Info result = new Info();

        Integer ans = (Integer) answer.get("a");

        if (ans == null) {
            result.put("result", 0);
            result.put("answer", ".");
        } else if (ans < 0) {
            result.put("result", 0);
            result.put("answer", ".");
        } else {
            ans = userAnswerToRealAnswer(ans, randSeed);
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
    public InfoPattern getCheckerPattern() {
        return new InfoPattern(
                "result", new BasicSerializationType<>(int.class), "Результат",
                "answer", new BasicSerializationType<>(String.class), "Ответ"
        );
    }

    @Override
    public String getType() {
        return "bebras";
    }

    @Override
    public Widget getWidget(boolean editor) {
        List<ResourceLink> links = new ArrayList<>();
        links.add(new ResourceLink("bebras.problem.css"));
        links.add(new ResourceLink("bebras.problem.js"));
        if (editor)
            links.add(new ResourceLink("bebras.edit.problem.js"));
        if (extraJS != null && !extraJS.trim().isEmpty())
            links.add(new EmbeddedLink(extraJS));

        return new ListWidget(links);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
        serializer.write("country", country);
        serializer.write("statement", statement);
        serializer.write("question", question);
        SerializationTypesRegistry.list(String.class).write(serializer, "answers", answers);
        serializer.write("answers count", answersCount);
        serializer.write("answers layout", answersLayout);
        serializer.write("right", rightAnswer);
        serializer.write("explanation", explanation);
        serializer.write("informatics", informatics);
        if (extraJS != null && !extraJS.trim().isEmpty())
            serializer.write("extrajs", extraJS);
    }

    @Override
    public void update(Deserializer deserializer) {
        title = deserializer.readString("title", "");
        country = deserializer.readString("country", "").toUpperCase();
        statement = deserializer.readString("statement", "");
        question = deserializer.readString("question", "");
        answers = SerializationTypesRegistry.list(String.class).read(deserializer, "answers");
        answersCount = deserializer.readInt("answers count", 4);
        answersLayout = deserializer.readInt("answers layout", 0);
        rightAnswer = deserializer.readInt("right", 0);
        explanation = deserializer.readString("explanation", "");
        informatics = deserializer.readString("informatics", "");
        extraJS = deserializer.readString("extrajs", "");

        while (answers.size() < answersCount)
            answers.add("");
    }

    public static String numberAnswer2string(int answer) {
        if (answer < 0)
            return "";
        return String.valueOf((char) ('А' + answer)); //русское А
    }

    // shuffling answers

    private int[] userAnswerToRealAnswerPermutation(long randSeed) {
        int[] result = new int[answersCount];

        if (randSeed == 0) {
            for (int i = 0; i < answersCount; i++)
                result[i] = i;
            return result;
        }

        Random random = new Random(randSeed);

        for (int i = 0; i < answersCount; i++) {
            result[i] = i;
            int j = random.nextInt(i + 1); //0 .. i

            int tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }

        return result;
    }

    private List<String> getUserAnswers(long randSeed) {
        int[] f = userAnswerToRealAnswerPermutation(randSeed);
        List<String> result = new ArrayList<>(answersCount);

        for (int i = 0; i < answersCount; i++)
            result.add(answers.get(f[i]));

        return result;
    }

    private int userAnswerToRealAnswer(int userAnswer, long randSeed) {
        int[] f = userAnswerToRealAnswerPermutation(randSeed);
        return f[userAnswer];
    }

    private int realAnswerToUserAnswer(int realAnswer, long randSeed) {
        int[] f = userAnswerToRealAnswerPermutation(randSeed);
        int[] g = new int[answersCount]; //we do not really need this array, but we will probably use it later
        for (int i = 0; i < answersCount; i++)
            g[f[i]] = i;

        return g[realAnswer];
    }

    private Html createAnswersHtml(List<String> userAnswers) {
        int cells = this.answersCount + 1;
        int rows = this.answersLayout;
        if (rows == 0)
            rows = cells;
        int cols = (int) Math.ceil((double) cells / rows);
        rows = (int) Math.ceil((double) cells / cols);

        List<String> htmlCells = IntStream
                .range(0, this.answersCount)
                .mapToObj(i -> answer_cell.render(i, userAnswers.get(i)).toString())
                .collect(Collectors.toList());

        htmlCells.add(no_answer_cell.render().toString());
        htmlCells.addAll(Collections.nCopies(rows * cols - htmlCells.size(), ""));

        String text = IntStream
                .iterate(0, i -> i + cols)
                .limit(rows)
                .mapToObj(i -> String.join("", htmlCells.subList(i, i + cols)))
                .map(s -> "<tr>" + s + "</tr>")
                .collect(Collectors.joining());

        return Html.apply(text);
    }
}
