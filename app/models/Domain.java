package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.forms.InputForm;
import models.newserialization.*;
import models.utils.Utils;
import play.cache.Cache;

import java.util.concurrent.Callable;

public class Domain implements SerializableUpdatable {

    public static final InputForm DOMAIN_CHANGE_FORM = InputForm.deserialize(
            new MemoryDeserializer(
                    "fields",
                    Utils.listify(
                            Utils.mapify(
                                    "name", "skin",
                                    "view", Utils.mapify(
                                            "type", "dropdown",
                                            "title", "Оформление",
                                            "placeholder", "Выберите тип оформления",
                                            "variants", Utils.listify("bbtc", "bebras")
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "defaultEvent",
                                    "view", Utils.mapify(
                                            "type", "string",
                                            "title", "Событие по умолчанию",
                                            "placeholder", "Введите идентификатор события по умолчанию"
                                    ),
                                    "required", true
                            ),
                            Utils.mapify(
                                    "name", "googleCounter",
                                    "view", Utils.mapify(
                                            "type", "multiline",
                                            "title", "Google counter",
                                            "placeholder", "Введите google counter"
                                    ),
                                    "required", false
                            ),
                            Utils.mapify(
                                    "name", "yandexMetrika",
                                    "view", Utils.mapify(
                                            "type", "multiline",
                                            "title", "Яндекс метрика",
                                            "placeholder", "Введите код яндекс метрики"
                                    ),
                                    "required", false
                            )
                    ),
                    "validators",
                    Utils.listify(

                    )
            )
    );

    private String name;
    private Mailer mailer = new Mailer();
    private String googleCounter = "";
    private String yandexMetrika = "";
    private String skin = "";
    private String defaultEvent = "default";

    public static Domain getInstance(final String name) {
        try {
            return Cache.getOrElse(domainCacheKey(name), new Callable<Domain>() {
                @Override
                public Domain call() throws Exception {
                    DBObject domainObject = MongoConnection.getDomainsCollection().findOne(new BasicDBObject("_id", name));
                    Domain domain = new Domain();
                    if (domainObject == null) {
                        domain.name = name;
                        domain.store();
                    } else
                        domain.update(new MongoDeserializer(domainObject));

                    return domain;
                }
            }, 0);
        } catch (Exception e) {
            Domain domain = new Domain();
            domain.name = name;
            return domain;
        }
    }

    private static String domainCacheKey(String name) {
        return "domain-cache-key-" + name;
    }

    public void store() {
        DBObject domainObject = new BasicDBObject();
        MongoSerializer serializer = new MongoSerializer(domainObject);
        serialize(serializer);
        MongoConnection.getDomainsCollection().save(domainObject);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("_id", name);
        serializer.write("mailer", mailer);
        serializer.write("googleCounter", googleCounter);
        serializer.write("yandexMetrika", yandexMetrika);
        serializer.write("skin", skin);
        serializer.write("defaultEvent", defaultEvent);
    }

    @Override
    public void update(Deserializer deserializer) {
        update(deserializer, false);
    }

    public void update(Deserializer deserializer, boolean fromForm) {
        if (!fromForm) {
            name = deserializer.readString("_id");

            mailer = new SerializableSerializationType<>(Mailer.class).read(deserializer, "mailer");

            if (mailer == null)
                mailer = new Mailer();
        }

        googleCounter = deserializer.readString("googleCounter", "");
        yandexMetrika = deserializer.readString("yandexMetrika", "");
        skin = deserializer.readString("skin", "");
        defaultEvent = deserializer.readString("defaultEvent", "");
    }

    public String getName() {
        return name;
    }

    public Mailer getMailer() {
        return mailer;
    }

    public String getGoogleCounter() {
        return googleCounter;
    }

    public String getYandexMetrika() {
        return yandexMetrika;
    }

    public String getSkin() {
        return skin;
    }

    public String getDefaultEvent() {
        return defaultEvent;
    }
}
