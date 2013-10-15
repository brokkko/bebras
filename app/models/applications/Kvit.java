package models.applications;

import models.User;
import models.Utils;
import models.results.Info;
import org.bson.types.ObjectId;
import plugins.Applications;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 23.09.13
 * Time: 15:51
 */
public class Kvit {

    public static final Kvit DEFAULT_KVIT = new Kvit(
            "АНО «КИО»",
            "7816365714 КПП 781601001",
            "40703810300050000015",
            Arrays.asList(
                    "Санкт-Петербургском ф-ле ОАО",
                    "«ФОНДСЕРВИСБАНК» г. Санкт-Петербург",
                    "к/с 30101810200000000717 БИК 044030717"
            )
    );

    private String kvitFileName;

    private String organization;
    private String innAndKpp;
    private String account;
    private List<String> bankNameAndAccount; //at most 3 elements

    public Kvit(String kvitFileName) {
        this.kvitFileName = kvitFileName;
    }

    public Kvit(String organization, String innAndKpp, String account, List<String> bankNameAndAccount) {
        this.organization = organization;
        this.innAndKpp = innAndKpp;
        this.account = account;
        this.bankNameAndAccount = bankNameAndAccount;
    }

    public static Kvit getKvitForUser(User user) {
        ObjectId regBy = user.getRegisteredBy();
        if (regBy == null)
            return DEFAULT_KVIT;
        return getKvitFromUserDescription(User.getInstance("_id", regBy));
    }

    public static Kvit getKvitFromUserDescription(User user) {
        if (user == null)
            return DEFAULT_KVIT;

        Info info = user.getInfo();

        String kvitFileName = (String) info.get("kvit file");
        if (kvitFileName != null)
            return new Kvit(kvitFileName);

        String organization = (String) info.get("kvit org");
        String innAndKpp = (String) info.get("kvit inn");
        String account = (String) info.get("kvit acc");
        String bankAsString = (String) info.get("kvit bank");
        List<String> bankNameAndAccount = bankAsString == null ? null : Arrays.asList(bankAsString.split("\\|"));

        if (organization == null || innAndKpp == null || account == null || bankNameAndAccount == null)
            return DEFAULT_KVIT;

        return new Kvit(organization, innAndKpp, account, bankNameAndAccount);
    }

    public String getKvitFileName() {
        return kvitFileName;
    }

    public String getOrganization() {
        return organization == null ? "" : organization;
    }

    public String getInnAndKpp() {
        return innAndKpp == null ? "" : innAndKpp;
    }

    public String getAccount() {
        return account == null ? "" : account;
    }

    public String getBankNameAndAccount(int line) {
        if (bankNameAndAccount == null)
            return "";
        if (line < 0 || line >= bankNameAndAccount.size())
            return "";
        return bankNameAndAccount.get(line);
    }

    public String getBankNameAndAccountAsOneLine() {
        String result = "";

        for (String item : bankNameAndAccount) {
            if (!result.isEmpty())
                result += " ";
            result += item;
        }

        return result;
    }

    public boolean isGenerated() {
        return kvitFileName == null;
    }

    public String getExtension() {
        return isGenerated() ? "PDF" : getExtension(kvitFileName);
    }

    private String getExtension(String kvitFileName) {
        int dotPos = kvitFileName.lastIndexOf('.');
        if (dotPos < 0)
            return "?";
        return kvitFileName.substring(dotPos + 1).toUpperCase();
    }

    public File generatePdfKvit(Applications plugin, Application application) throws IOException, InterruptedException {
        File page1 = File.createTempFile("pdf-kvit-", "-pd4-1.html");
        File page2 = File.createTempFile("pdf-kvit-", "-pd4-2.html");
        File css = File.createTempFile("pdf-kvit-", "-pd4.css");
        File pdf = File.createTempFile("pdf-kvit-", "-pd4.pdf");

        Map<Object, Object> subs = Utils.mapify(
                "{css}", css.getName(),
                "{price}", plugin.getApplicationPrice(application),
                "{pay_for}", "Регистрационный взнос " + plugin.getTypeByName(application.getType()).getDescription(),
                "{packet_name}", application.getName(),

                "{org}", getOrganization(),
                "{inn}", getInnAndKpp(),
                "{line1}", getBankNameAndAccount(0),
                "{line2}", getBankNameAndAccount(1),
                "{line3}", getBankNameAndAccount(2),
                "{acc}", getAccount()
        );
        Utils.writeResourceToFile("/public/invoice-1.html", page1, subs);
        Utils.writeResourceToFile("/public/invoice-2.html", page2, subs);
        Utils.writeResourceToFile("/public/invoice.css", css);

        Utils.runProcess("/opt/wkhtmltopdf-amd64", page1.getAbsolutePath(), page2.getAbsolutePath(), pdf.getAbsolutePath());

        return pdf;
    }

}
