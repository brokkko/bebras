package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.kio.KioCertificateFactory;
import plugins.certificates.kio.KioDiploma;
import plugins.certificates.kio.KioDiplomaFactory;

public class DiplomaFactorySerializationType extends SerializableTreeSerializationType<DiplomaFactory> {

    public DiplomaFactorySerializationType() {
        registerClass("kio certificate", KioCertificateFactory.class);
        registerClass("kio diploma", KioDiplomaFactory.class);
    }
}
