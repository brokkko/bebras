package models.newproblems.example;

import models.forms.RawForm;
import models.newproblems.Problem;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import play.twirl.api.Html;
import views.widgets.Widget;

import java.util.Comparator;

public class ExampleProblem implements Problem {

    private String statement;
    private Color color;
    private String answer;

    /**
     * Возвращает HTML задачи, кусок, который вставляется в {@code <div class="problem"></div>},
     * поэтому этот HTML код должен быть независим от остального кода. На одной странице могут отображаться
     * несколько одинаковых задач, поэтому нельзя использовать глобальное состояние.
     * @param index порядковый номер задачи в соревновании. Задачи сами оформляют свой номер, если хотят.
     * @param showSolutions нужно ли отображать решение. Если задача отображается во время соревнования,
     *                      то сюда передается false, если задача используется при подведении итогов и отображении
     *                      результатов, сюда может быть передано true
     * @param settings Ассоциативный массив с настройками задачи. Здесь вся информация, которая необходима
     *                 для отображения данного типа задач
     * @param seed Если в задаче есть генерация, то это число используется для инициализации генератора случайных
     *             чисел. При одном значении seed задача должна получаться одна и та же.
     * @return возвращается HTML с задачей для вставки в div.
     */
    @Override
    public Html format(String index, boolean showSolutions, Info settings, long seed) {
        return null;
    }

    /**
     * Функция не нужна, пусть возвращает true
     * @return true
     */
    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public Html formatEditor() {
        return null;
        //TODO implement
    }

    /**
     * Функция обновляет задачу по данным, полученным из редактора. Фактически, от автора задачи приходит
     * POST запрос с данными, введенными в редакторе задачи, и в этом методе задача обновляет себя на основе полученных
     * данных
     * @param form форма с данными, введенными в браузере.
     */
    @Override
    public void updateProblem(RawForm form) {
        statement = form.get("statement");
        color = Color.valueOf(form.get("color"));
        answer = form.get("answer");
    }

    @Override
    public String answerToString(Info answer, long randSeed) {
        return null;
    }

    @Override
    public String answerString() {
        return null;
    }

    @Override
    public Info check(Info answer, long randSeed) {
        return null;
    }

    @Override
    public InfoPattern getAnswerPattern() {
        return null;
    }

    @Override
    public InfoPattern getCheckerPattern() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public Widget getWidget(boolean editor) {
        return null;
    }

    @Override
    public void serialize(Serializer serializer) {

    }

    @Override
    public void update(Deserializer deserializer) {

    }

    @Override
    public Comparator<Info> comparator() {
        return null;
    }
}
