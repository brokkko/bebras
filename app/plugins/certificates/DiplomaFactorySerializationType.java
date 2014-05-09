package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.kio.KioCertificateFactory;
import plugins.certificates.kio.KioDiploma;
import plugins.certificates.kio.KioDiplomaFactory;
import plugins.certificates.kio.KioProblemDiplomaFactory;

public class DiplomaFactorySerializationType extends SerializableTreeSerializationType<DiplomaFactory> {

    public DiplomaFactorySerializationType() {
        registerClass("kio certificate", KioCertificateFactory.class);
        registerClass("kio diploma", KioDiplomaFactory.class);
        registerClass("kio problem diploma", KioProblemDiplomaFactory.class);
    }
}
