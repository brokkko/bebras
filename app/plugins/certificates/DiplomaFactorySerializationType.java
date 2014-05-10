package plugins.certificates;

import models.newserialization.SerializableTreeSerializationType;
import plugins.certificates.kio.*;

public class DiplomaFactorySerializationType extends SerializableTreeSerializationType<DiplomaFactory> {

    public DiplomaFactorySerializationType() {
        registerClass("kio certificate", KioCertificateFactory.class);
        registerClass("kio diploma", KioDiplomaFactory.class);
        registerClass("kio problem diploma", KioProblemDiplomaFactory.class);
        registerClass("kio teacher gramota", KioTeacherGramotaFactory.class);
    }
}
