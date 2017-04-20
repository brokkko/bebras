package models.results;

import models.newserialization.SerializableTreeSerializationType;
import models.results.kio.Kio14Translator;
import models.results.kio.KioJSTranslator;
import models.results.kio.KioLevelTranslator;
import models.results.kio.KioTranslator;

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
        registerClass("kiojs", KioJSTranslator.class);
        registerClass("kio level", KioLevelTranslator.class);
    }
}
