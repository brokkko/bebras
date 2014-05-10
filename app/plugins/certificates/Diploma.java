package plugins.certificates;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.*;
import models.ServerConfiguration;
import models.User;
import models.results.Info;
import play.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.PrivateKey;

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
            results = user.getContestResults(user.getEvent().getContestById(contestId));
    }

    protected Info getResults() {
        if (results == null)
            results = user.getEventResults();
        return results;
    }

    protected String getResult(String field) {
        return (String) getResults().get(field);
    }

    public abstract int getWidthsInMM();

    public abstract int getHeightInMM();

    public abstract String bgPath();

    public abstract boolean isHonored();

    public abstract void draw(PdfWriter writer);

    public File createPdf() {
        final Document doc = new Document(
                new Rectangle(
                        Utilities.millimetersToPoints(getWidthsInMM()), Utilities.millimetersToPoints(getHeightInMM())
                ),
                0, 0, 0, 0
        );

        //TODO report "never used"
        try (AutoCloseable ignored = new AutoCloseable() {
            @Override
            public void close() throws Exception {
                doc.close();
            }
        }) {
            File outputPath = File.createTempFile("pdf-certificate-", ".pdf");

            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));

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