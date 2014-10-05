package plugins.bebraspdf.generator;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import plugins.bebraspdf.model.KeyFieldConsts;

import java.io.*;

/**
 * Всталвяет титульную страницу и чек-боксы
 */
public class TaskPdfCreator {

    public static final String[] ANSWERS = {"А", "Б", "В", "Г", "Не знаю"};

    /**
     * Создает динамический pdf
     *
     * @param inputStream  - поток для исходного макета
     * @param outputStream - поток для результирующего файла
     * @param firstClass   - первый возможный класс ("3")
     * @param secondClass  - второй возможный класс ("4")
     * @throws IOException
     * @throws DocumentException
     */
    public void makePdf(InputStream inputStream, OutputStream outputStream, String firstClass, String secondClass) throws IOException, DocumentException {

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        PdfContentByte contentByte = writer.getDirectContent();

        new TitleCreator().appendTitle(document, writer, firstClass, secondClass);

        PdfReader reader = new PdfReader(inputStream);
        int amountOfPages = reader.getNumberOfPages();
        for (int i = 1; i < amountOfPages + 1; i++) {
            PdfImportedPage page = writer.getImportedPage(reader, i);
            document.newPage();
            contentByte.addTemplate(page, 0, 0);
            drawRadioButtons(writer, contentByte, i);
        }
        writer.setViewerPreferences(PdfWriter.PageModeUseThumbs | PdfWriter.PageLayoutSinglePage);
        document.close();
    }

    private void drawRadioButtons(PdfWriter writer, PdfContentByte contentByte, int taskNumber) throws IOException, DocumentException {
        int startY = 40;
        int startX = 55;
        int xSpace = 35;
        PdfFormField radiogroup = PdfFormField.createRadioButton(writer, true);
        radiogroup.setFieldName(KeyFieldConsts.TASK + taskNumber);
        ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT,
                new Phrase("Ваш ответ:", new Font(GeneratorUtils.getBaseFont(), 18, Font.BOLD)), startX, startY, 0);
        for (int j = 0; j < ANSWERS.length; j++) {
            Rectangle rect = new Rectangle(startX + 125 + j * xSpace, startY - 5, startX + 105 + j * xSpace, startY - 25);
            if (j == ANSWERS.length - 1) {
                rect = new Rectangle(startX + 125 + j * xSpace + 60, startY - 5, startX + 105 + j * xSpace, startY - 25);
            }
            RadioCheckField radio = new RadioCheckField(writer, rect, j + "", j + "");
            radio.setBorderColor(GrayColor.GRAYBLACK);
            radio.setBackgroundColor(GrayColor.GRAYWHITE);
            radio.setCheckType(RadioCheckField.TYPE_CIRCLE);
            PdfFormField field = radio.getRadioField();
            radiogroup.addKid(field);
            ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT,
                    new Phrase(ANSWERS[j], new Font(GeneratorUtils.getBaseFont(), 18)), startX + 109 + j * xSpace, startY, 0);
        }
        writer.addAnnotation(radiogroup);
        contentByte.saveState();
        contentByte.setColorStroke(BaseColor.BLACK);
        contentByte.rectangle(startX - 15, startY - 30, 350, 50);
        contentByte.stroke();
        contentByte.restoreState();
    }


    public static void main(String[] args) throws Exception {

//        Вставляем титульную страницу и чек-боксы
        File folder = new File("D:\\contest\\contest");
        File[] files = folder.listFiles();

        if (files == null)
            throw new Exception("failed to list folder" + folder);

        for (File file : files) {
            String path = file.getAbsolutePath();
            if (path.contains("-")) {
                int index = path.indexOf("-");
                String firstClass = path.substring(index - 1, index);
                String secondClass = path.substring(index + 1, index + 2);
                if (secondClass.equals("1")) {
                    secondClass = "10";
                }
                new TaskPdfCreator().makePdf(new FileInputStream(path), new FileOutputStream(path.substring(0, path.length() - 4) + "result" + ".pdf"), firstClass, secondClass);
            } else {
                new TaskPdfCreator().makePdf(new FileInputStream(path), new FileOutputStream(path.substring(0, path.length() - 4) + "result" + ".pdf"), "11", "11");
            }
        }

    }

}

