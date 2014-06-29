package plugins.bebraspdf.generator;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import plugins.bebraspdf.model.KeyFieldConsts;

import java.io.IOException;

/**
 * Создает титульную страницу
 */
public class TitleCreator {

    public void appendTitle(Document document, PdfWriter writer, String firstClass, String secondClass) throws DocumentException, IOException {
        PdfPTable headerTable = createTextTable("Пробный тур конкурса Бобёр 2013", 22);
        headerTable.setSpacingAfter(20);
        document.add(headerTable);

        PdfPTable classHeaderTable;
        if(!firstClass.equals(secondClass)){
            classHeaderTable = createTextTable(firstClass+"-"+secondClass+" классы", 22);
            addMeta(document, KeyFieldConsts.YEAR_INTERVAL, firstClass+"-"+secondClass);
        }else{
            classHeaderTable = createTextTable("11 класс", 22);
            addMeta(document, KeyFieldConsts.YEAR_INTERVAL, "11");
        }
        classHeaderTable.setSpacingAfter(50);
        document.add(classHeaderTable);

        addPicture(document, "D:\\contest\\logo.png", 100);

        PdfPTable surnameTable = createFormTable("Введите свою фамилию:  ", "", KeyFieldConsts.SURNAME);
        surnameTable.setSpacingAfter(20);
        document.add(surnameTable);

        PdfPTable nameTable = createFormTable("Введите свое имя:  ", "", KeyFieldConsts.NAME);
        if(!firstClass.equals(secondClass)){
            nameTable.setSpacingAfter(20);
        }else{
            nameTable.setSpacingAfter(60);
        }
        document.add(nameTable);


        if(!firstClass.equals(secondClass)){
            PdfPTable classTable = createFormTableRadio("Укажите свой класс:  ", KeyFieldConsts.CLASS, writer, new String[]{firstClass, secondClass});
            classTable.setSpacingAfter(40);
            document.add(classTable);
        }


        PdfPTable heplerHeader = createTextTable("Выбирайте ответы внизу страниц с условиями задач", 14);
        heplerHeader.setSpacingAfter(30);
        document.add(heplerHeader);


        PdfPTable helperHeader2 = createTextTable("После решения задач сохраните файл и передайте учителю", 14);
        document.add(helperHeader2);
    }

    private PdfPTable createFormTableRadio(String label, String name, PdfWriter writer, String[] items) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setWidths(new int[]{ 10, 15 });

        Phrase phrase = new Phrase(label, new Font(GeneratorUtils.getBaseFont(), 14));
        PdfPCell labelCell = new PdfPCell(phrase);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setBorderWidth(0);
        table.addCell(labelCell);

        PdfPCell fieldCell = new PdfPCell();
        fieldCell.setBorderWidth(0);

        int y = 220;
        int x = 155;
        int xSpace = 35;

        PdfFormField radiogroup = PdfFormField.createRadioButton(writer, true);
        radiogroup.setFieldName(name);
        PdfContentByte cb = writer.getDirectContent();
        for (int j = 0; j < items.length; j++) {
            Rectangle rect = new Rectangle(x+140+j*xSpace+(items[j].length()-1)*17, y+15, x+120+j*xSpace, y-5);
            RadioCheckField radio = new RadioCheckField(writer, rect, j+"", j+"");
            radio.setBorderColor(GrayColor.GRAYBLACK);
            radio.setBackgroundColor(GrayColor.GRAYWHITE);
            radio.setCheckType(RadioCheckField.TYPE_CIRCLE);
            PdfFormField field = radio.getRadioField();
            radiogroup.addKid(field);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(items[j], new Font(GeneratorUtils.getBaseFont(), 14)), x+109+j*xSpace, y, 0);
        }
        writer.addAnnotation(radiogroup);
        table.addCell(fieldCell);
        return table;
    }

    private void addPicture(Document document, String path, int spaceAfter) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidths(new int[]{1});

        PdfPCell labelCell = new PdfPCell(Image.getInstance(path));
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setBorderWidth(0);
        headerTable.addCell(labelCell);
        headerTable.setSpacingAfter(spaceAfter);

        document.add(headerTable);
    }


    private void addMeta(Document document, String key, String value) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidths(new int[]{1});

        PdfPCell fieldCell = new PdfPCell();
        fieldCell.setCellEvent(new SimpleTextField(key, value, false));
        fieldCell.setBorder(0);
        headerTable.addCell(fieldCell);

        document.add(headerTable);
    }


    private PdfPTable createTextTable(String label, int size) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidths(new int[]{1});

        Phrase phrase = new Phrase(label, new Font(GeneratorUtils.getBaseFont(), size));
        PdfPCell labelCell = new PdfPCell(phrase);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setBorderWidth(0);
        headerTable.addCell(labelCell);

        return headerTable;
    }



    private PdfPTable createFormTable(String label, String initialValue, String name) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidths(new int[]{ 10, 13 });

        Phrase phrase = new Phrase(label, new Font(GeneratorUtils.getBaseFont(), GeneratorUtils.DEFAULT_FONT_SIZE));
        PdfPCell labelCell = new PdfPCell(phrase);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setVerticalAlignment(Element.ALIGN_CENTER);
        labelCell.setBorderWidth(0);
        labelCell.setRightIndent(10);
        table.addCell(labelCell);

        PdfPCell fieldCell = new PdfPCell();
        fieldCell.setCellEvent(new SimpleTextField(name, initialValue, true));
        fieldCell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(fieldCell);
        return table;
    }


}
