package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.bebras.BabiorCertificateFactory;
import plugins.certificates.bebras.BebrasDiplomaFactory;
import plugins.certificates.bebras.BebrasKazanCertificate;
import plugins.certificates.daedal.DaedalDiplomaFactory;
import plugins.certificates.dmti.DmTiCertificateFactory;
import plugins.certificates.dmti.ThankYouLetterFactory;
import plugins.certificates.kio.*;

public class DiplomaFactorySerializationType extends SerializableTreeSerializationType<DiplomaFactory> {

    public DiplomaFactorySerializationType() {
        registerClass("kio certificate", KioCertificateFactory.class);
        registerClass("kio diploma", KioDiplomaFactory.class);
        registerClass("kio problem diploma", KioProblemDiplomaFactory.class);
        registerClass("kio teacher gramota", KioTeacherGramotaFactory.class);
        registerClass("kio teacher certificate", KioTeacherCertificateFactory.class);
        registerClass("dm ti thank you letter", ThankYouLetterFactory.class);
        registerClass("dm ti certificate", DmTiCertificateFactory.class);
        registerClass("bebras diploma", BebrasDiplomaFactory.class);
        registerClass("babior certificate", BabiorCertificateFactory.class);
        registerClass("daedal", DaedalDiplomaFactory.class);
    }
}
