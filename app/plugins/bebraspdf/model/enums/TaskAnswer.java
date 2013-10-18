package plugins.bebraspdf.model.enums;

/**
 * Список возможных ответов
 *
 * @author Vasiliy
 * @date 18.10.13
 */
public enum TaskAnswer {
    FIRST("1", "0"),
    SECOND("2", "1"),
    THIRD("3", "2"),
    FOURTH("4", "3"),
    DONTKNOW("Не знаю", "4"),
    UNDEFINED("Не выбрано", "");

    private final String name;

    private final String pdfValue;

    TaskAnswer(String name, String pdfValue) {
        this.name = name;
        this.pdfValue = pdfValue;
    }

    public String getName() {
        return name;
    }

    public String getPdfValue() {
        return pdfValue;
    }

    public static TaskAnswer getTaskAnswerByPdfValue(String pdfValue) {
        for (TaskAnswer taskAnswer : TaskAnswer.values()) {
            if (taskAnswer.getPdfValue().equals(pdfValue)) {
                return taskAnswer;
            }
        }
        return null;
    }
}
