package plugins.certificates;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.*;
import models.Contest;
import models.ServerConfiguration;
import models.User;
import models.results.Info;
import play.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

public abstract class Diploma<Factory extends DiplomaFactory> {

    protected User user;
    protected Factory factory;
    private Info results = null;

    @Deprecated
    protected Diploma(User user) {
        this.user = user;
    }

    protected Diploma(User user, Factory factory) {
        this.user = user;
        this.factory = factory;

        String contestId = factory.getContestId();
        if (contestId != null)
            results = getContestResults(user, contestId);
        else {
            List<String> contestIDs = factory.getContestIds();
            if (contestIDs != null)
                results = findResultsForContestsInList(contestIDs);
        }
    }

    private Info getContestResults(User user, String contestId) {
        return user.getContestResults(user.getEvent().getContestById(contestId));
    }

    private Info findResultsForContestsInList(List<String> contestIDs) {
        for (String contestID : contestIDs) {
            Contest contest = user.getEvent().getContestById(contestID);
            if (contest == null)
                continue;
            if (contest.isAvailableForUser(user))
                return getContestResults(user, contestID);
        }

        return null;
    }

    protected Info getResults() {
        if (results == null)
            results = user.getEventResults();
        return results;
    }

    protected String getResult(String field) {
        Object res = getResults().get(field);
        return res == null ? null : String.valueOf(res);
    }

    protected Info getResults(String contestId) {
        if (contestId == null || contestId.isEmpty())
            return  getResults();
        return user.getContestResults(user.getEvent().getContestById(contestId));
    }

    protected String getResult(String field, String contestId) {
        Object res = getResults(contestId).get(field);
        return res == null ? null : String.valueOf(res);
    }

    public abstract int getWidthsInMM();

    public abstract int getHeightInMM();

    public abstract String bgPath();

    public abstract boolean isHonored();

    public abstract void draw(PdfWriter writer);

    public User getUser() {
        return user;
    }

    public File createPdf() {
        final Document doc = new Document(
                new Rectangle(
                        Utilities.millimetersToPoints(getWidthsInMM()), Utilities.millimetersToPoints(getHeightInMM())
                ),
                0, 0, 0, 0
        );

        //TODO report "never used"
        try (AutoCloseable ignored = doc::close) {
            File outputPath = File.createTempFile("pdf-certificate-", ".pdf");

            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath)); //TODO do we need to close the writer?

            Image bgImage = null;

            String bg = bgPath();
            if (bg != null) {
                if (bg.contains("://"))
                    bgImage = Image.getInstance(new URL(bg));
                else
                    bgImage = Image.getInstance(bg);
                bgImage.setAbsolutePosition(0, 0);
                bgImage.scaleAbsolute(Utilities.millimetersToPoints(getWidthsInMM()), Utilities.millimetersToPoints(getHeightInMM()));
            }

            doc.open();

            doc.newPage();
            if (bgImage != null)
                doc.add(bgImage);

            draw(writer);

            doc.close();

            //TODO it should be possible to sign a document in the process of creation
            /*File signedOutputPath = File.createTempFile("pdf-certificate-", ".pdf");
            signPdf(outputPath, signedOutputPath);

            return signedOutputPath;*/

            return outputPath;

        } catch (Exception e) {
            Logger.error("Error while creating certificate", e);
        }

        return null;
    }

//    taken from http://itextpdf.com/examples/iia.php?id=222
    private void signPdf(File inputPath, File outputPath) throws Exception {
        File path = ServerConfiguration.getInstance().getPluginFile(DiplomaPlugin.PLUGIN_NAME, ".keystore");
        String keystore_password = "password";
        String key_password = "password";
        KeyStore ks = KeyStore.getInstance("pkcs12"/*, "BC"*/);
        ks.load(new FileInputStream(path), keystore_password.toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, key_password.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);
        // reader and stamper
        PdfReader reader = new PdfReader(inputPath.getAbsolutePath());
        FileOutputStream os = new FileOutputStream(outputPath);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//        appearance.setImage(Image.getInstance(RESOURCE));
        appearance.setReason("Contest results paper");
        appearance.setLocation("Saint Petersburg");
//        appearance.setVisibleSignature(new Rectangle(72, 732, 144, 780), 1,    "first");
        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", null);
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
    }
}