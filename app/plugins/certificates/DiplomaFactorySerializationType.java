package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.dmti.DmTiCertificate;
import plugins.certificates.dmti.DmTiCertificateFactory;
import plugins.certificates.dmti.ThankYouLetter;
import plugins.certificates.dmti.ThankYouLetterFactory;
import plugins.certificates.kio.*;

public class DiplomaFactorySerializationType extends SerializableTreeSerializationType<DiplomaFactory> {

    public DiplomaFactorySerializationType() {
        registerClass("kio certificate", KioCertificateFactory.class);
        registerClass("kio diploma", KioDiplomaFactory.class);
        registerClass("kio problem diploma", KioProblemDiplomaFactory.class);
        registerClass("kio teacher gramota", KioTeacherGramotaFactory.class);
        registerClass("dm ti thank you letter", ThankYouLetterFactory.class);
        registerClass("dm ti certificate", DmTiCertificateFactory.class);
    }
}
