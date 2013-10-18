package plugins.bebraspdf.model.enums;

/**
 * Список исходных файлов, которые выдаются участникам
 *
 * @author Vasiliy
 * @date 18.10.13
 */
public enum PdfFile {
    FIRST_SECOND("1-2", 1),
    THIRD_FOURTH("3-4", 3),
    FIFTH_SIXTH("5-6", 5),
    SEVENTH_EIGHTN("7-8", 7),
    NINHT_TENTH("9-10", 9),
    ELEVENTH("11", 11);

    private final String name;

    private final int startClass;

    PdfFile(String name, int startClass) {
        this.name = name;
        this.startClass = startClass;
    }

    public String getName() {
        return name;
    }

    public int getStartClass() {
        return startClass;
    }

    public static PdfFile getFileByName(String name) {
        for (PdfFile pdfFile : PdfFile.values()) {
            if (pdfFile.getName().equals(name)) {
                return pdfFile;
            }
        }
        return null;
    }
}
