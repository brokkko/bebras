package models.newserialization;

import models.data.ObjectsProviderFactorySerializationType;
import models.forms.InputTemplateSerializationType;
import models.forms.validators.ValidatorSerializationType;
import models.newproblems.ProblemSerializationType;
import models.results.TranslatorSerializationType;
import plugins.PluginSerializationType;
import plugins.certificates.DiplomaFactorySerializationType;

/**
 * Created by ilya
 */
public class SerializationTypesRegistry {

    public static final TranslatorSerializationType TRANSLATOR = new TranslatorSerializationType();

    public static final InputTemplateSerializationType INPUT_TEMPLATE = new InputTemplateSerializationType();

    public static final ValidatorSerializationType VALIDATOR = new ValidatorSerializationType();

    public static final ObjectsProviderFactorySerializationType OBJECTS_PROVIDER_FACTORY = new ObjectsProviderFactorySerializationType();

    public static final ProblemSerializationType PROBLEM = new ProblemSerializationType();

    public static final PluginSerializationType PLUGIN = new PluginSerializationType();

    public static final DiplomaFactorySerializationType CERTIFICATE_FACTORY = new DiplomaFactorySerializationType();

    /*public static <T> ArraySerializationType<T> array(SerializationType<T> subtype) {
        return new ArraySerializationType<>(subtype);
    }

    public static <T> ArraySerializationType<T> array(Class<T> clazz) {
        return new ArraySerializationType<>(new BasicSerializationType<T>(clazz));
    }

    //TODO find out what is the problem with reading arrays: java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to [Lorg.bson.types.ObjectId;

    */

    public static <T> ListSerializationType<T> list(SerializationType<T> subtype) {
        return new ListSerializationType<>(subtype);
    }

    public static <T> ListSerializationType<T> list(Class<T> clazz) {
        return new ListSerializationType<>(new BasicSerializationType<T>(clazz));
    }

    public static <T> StringMapSerializationType<T> map(SerializationType<T> subtype) {
        return new StringMapSerializationType<>(subtype);
    }

    public static <T> StringMapSerializationType<T> map(Class<T> clazz) {
        return new StringMapSerializationType<>(new BasicSerializationType<>(clazz));
    }
}
