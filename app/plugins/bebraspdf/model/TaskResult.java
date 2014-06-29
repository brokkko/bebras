package plugins.bebraspdf.model;

import plugins.bebraspdf.model.enums.TaskAnswer;

/**
 * @author Vasiliy
 * @date 18.10.13
 */
public class TaskResult {
    private int taskNumber;
    private TaskAnswer answer;

    public TaskResult(int taskNumber, TaskAnswer answer) {
        this.taskNumber = taskNumber;
        this.answer = answer;
    }

    public int getTaskNumber() {
        return taskNumber;
    }

    public TaskAnswer getAnswer() {
        return answer;
    }
}
