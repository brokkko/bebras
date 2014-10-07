package plugins.bebraspdf.model;

import plugins.bebraspdf.model.enums.PdfFile;
import plugins.bebraspdf.model.enums.UserClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Результат работы участника
 * @author Vasiliy
 * @date 18.10.13
 */
public class UserResult {

    private PdfUser pdfUser;

    private PdfFile pdfFile;

    private UserClass userClass;

    private List<TaskResult> taskResults = new ArrayList<>();

    public UserResult(PdfUser pdfUser, PdfFile pdfFile, UserClass userClass) {
        this.pdfUser = pdfUser;
        this.pdfFile = pdfFile;
        this.userClass=userClass;
    }

    public void addTaskResult(TaskResult taskResult){
        taskResults.add(taskResult);
    }

    public PdfUser getPdfUser() {
        return pdfUser;
    }

    public PdfFile getPdfFile() {
        return pdfFile;
    }

    public UserClass getUserClass() {
        return userClass;
    }

    public List<? extends TaskResult> getTaskResults() {
        return taskResults;
    }
}
