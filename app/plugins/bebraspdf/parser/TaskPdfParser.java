package plugins.bebraspdf.parser;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import plugins.bebraspdf.model.KeyFieldConsts;
import plugins.bebraspdf.model.TaskResult;
import plugins.bebraspdf.model.PdfUser;
import plugins.bebraspdf.model.UserResult;
import plugins.bebraspdf.model.enums.PdfFile;
import plugins.bebraspdf.model.enums.TaskAnswer;
import plugins.bebraspdf.model.enums.UserClass;

import java.io.IOException;
import java.io.InputStream;

/**
 * Получает данные из документа
 */
public class TaskPdfParser {

    public UserResult getResult(String path) throws IOException {
        return getUserResult(new PdfReader(path));
    }

    public UserResult getResult(InputStream inputStream) throws IOException {
        return getUserResult(new PdfReader(inputStream));
    }

    private UserResult getUserResult(PdfReader reader) {
        AcroFields form = reader.getAcroFields();

        PdfUser pdfUser = new PdfUser(form.getField(KeyFieldConsts.NAME), form.getField(KeyFieldConsts.SURNAME));
        PdfFile pdfFile = PdfFile.getFileByName(form.getField(KeyFieldConsts.YEAR_INTERVAL));

        String fieldValue = form.getField(KeyFieldConsts.CLASS);
        int fieldIntValue = fieldValue == null || fieldValue.isEmpty() ? 0 : Integer.parseInt(fieldValue);
        UserClass userClass = UserClass.getUserClassByClassNumber(pdfFile.getStartClass() + fieldIntValue);

        UserResult userResult = new UserResult(pdfUser, pdfFile, userClass);

        for (int i = 1; i < KeyFieldConsts.TASK_AMOUNT + 1; i++) {
            TaskResult taskResult = new TaskResult(i, TaskAnswer.getTaskAnswerByPdfValue(form.getField(KeyFieldConsts.TASK + i)));
            userResult.addTaskResult(taskResult);
        }

        reader.close();
        return userResult;
    }

}
