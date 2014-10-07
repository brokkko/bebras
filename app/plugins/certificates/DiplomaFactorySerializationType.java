package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.dmti.DmTiCertificateFactory;
import plugins.certificates.dmti.ThankYouLetterFactory;
import plugins.certificates.kio.KioCertificateFactory;
import plugins.certificates.kio.KioDiplomaFactory;
import plugins.certificates.kio.KioProblemDiplomaFactory;
import plugins.certificates.kio.KioTeacherGramotaFactory;

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
