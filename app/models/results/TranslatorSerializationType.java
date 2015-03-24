package models.results;

import models.newserialization.SerializableTreeSerializationType;

/**
 * Created by ilya
 */
public class TranslatorSerializationType extends SerializableTreeSerializationType<Translator> {

    public TranslatorSerializationType() {
        registerClass("beaver", BeaverTranslator.class);
        registerClass("empty", EmptyTranslator.class);
        registerClass("sum scores", SumScoresTranslator.class);
        registerClass("transfer", TransferTranslator.class);
        registerClass("kio14", Kio14Translator.class);
        registerClass("kio", KioTranslator.class);
    }
}
