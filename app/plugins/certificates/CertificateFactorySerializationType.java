package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.kio.KioCertificateFactory;

public class CertificateFactorySerializationType extends SerializableTreeSerializationType<DiplomaFactory> {

    public CertificateFactorySerializationType() {
        registerClass("kio certificate", KioCertificateFactory.class);
    }
}
