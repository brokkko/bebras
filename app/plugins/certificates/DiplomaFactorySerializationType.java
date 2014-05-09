package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.kio.KioCertificateFactory;

public class DiplomaFactorySerializationType extends SerializableTreeSerializationType<DiplomaFactory> {

    public DiplomaFactorySerializationType() {
        registerClass("kio certificate", KioCertificateFactory.class);
    }
}
