package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newserialization.*;
import play.api.templates.Html;
import sun.util.logging.resources.logging_de;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.01.13
 * Time: 13:49
 */
public class DropdownInputTemplate extends InputTemplate<String> {

    private String placeholder;
    private List<String> variants;
    private List<String> titles;

    private String load;
    private List<String> extraVariants;
    private List<String> extraTitles;

    @Override
    public Html render(RawForm form, String field) {
        return views.html.fields.dropdown.render(form, field, placeholder, extraVariants, extraTitles);
    }

    @Override
    public void write(String field, String value, RawForm rawForm) {
        if (value != null)
            rawForm.put(field, value);
        else
            rawForm.remove(field);
    }

    @Override
    public String read(String field, RawForm form) {
        String value = form.get(field);
        if (value != null && value.isEmpty())
            return null;
        return value;
    }

    @Override
    public SerializationType<String> getType() {
        return new BasicSerializationType<>(String.class);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        placeholder = deserializer.readString("placeholder", "");
        variants = SerializationTypesRegistry.list(String.class).read(deserializer, "variants");
        titles = SerializationTypesRegistry.list(String.class).read(deserializer, "titles");

        if (titles == null || titles.size() == 0)
            titles = variants;

        load = deserializer.readString("load");

        load(load);
    }

    private void load(String load) {
        extraVariants = new ArrayList<>(variants);
        extraTitles = new ArrayList<>(titles);

        if (load == null)
            return;

        switch (load) {
            case "regions":
                extraVariants.addAll(REGION_VARIANTS);
                extraTitles.addAll(REGION_TITLES);
        }
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        if (placeholder != null && !placeholder.isEmpty())
            serializer.write("placeholder", placeholder);
        SerializationTypesRegistry.list(String.class).write(serializer, "variants", variants);
        if (titles != variants)
            SerializationTypesRegistry.list(String.class).write(serializer, "titles", titles);

        if (load != null)
            serializer.write("load", load);
    }

    private static List<String> REGION_VARIANTS = Arrays.asList(
            "~group",
            "MSK",
            "SPB",
            "~group",
            "AMR",
            "ARK",
            "AST",
            "BEL",
            "BRY",
            "VLA",
            "VLG",
            "VOL",
            "VOR",
            "IVN",
            "IRK",
            "KLN",
            "KLZ",
            "KEM",
            "KIR",
            "KOS",
            "KRG",
            "KRS",
            "LEN",
            "LIP",
            "MAG",
            "MOS",
            "MUR",
            "NIZ",
            "NVG",
            "NVS",
            "OMS",
            "ORN",
            "ORL",
            "PEN",
            "PSK",
            "ROS",
            "RAS",
            "SAM",
            "SAR",
            "SAH",
            "SVE",
            "SMO",
            "TMB",
            "TVR",
            "TMS",
            "TUL",
            "TUM",
            "ULJ",
            "CHL",
            "YAR",
            "~group",
            "ALT",
            "ZAB",
            "KMC",
            "KRD",
            "KRY",
            "PER",
            "PRI",
            "STR",
            "HAB",
            "~group",
            "ADG",
            "ALR",
            "BSH",
            "BUR",
            "DAG",
            "ING",
            "KAB",
            "KLM",
            "KCH",
            "KAR",
            "KOM",
            "MEL",
            "MOR",
            "SAH",
            "SOA",
            "TAT",
            "TYV",
            "UDM",
            "HAK",
            "CHC",
            "CHV",
            "~group",
            "EAO",
            "NAO",
            "HAO",
            "CAO",
            "YNO",
            "~group",
            "KZH",
            "BLR",
            "UKR"
    );

    private static List<String> REGION_TITLES = Arrays.asList(
            "город фед. значения",
            "Москва",
            "Санкт-Петербург",
            "область",
            "Амурская",
            "Архангельская",
            "Астраханская",
            "Белгородская",
            "Брянская",
            "Владимирская",
            "Волгоградская",
            "Вологодская",
            "Воронежская",
            "Ивановская",
            "Иркутская",
            "Калининградская",
            "Калужская",
            "Кемеровская",
            "Кировская",
            "Костромская",
            "Курганская",
            "Курская",
            "Ленинградская",
            "Липецкая",
            "Магаданская",
            "Московская",
            "Мурманская",
            "Нижегородская",
            "Новгородская",
            "Новосибирская",
            "Омская",
            "Оренбургская",
            "Орловская",
            "Пензенская",
            "Псковская",
            "Ростовская",
            "Рязанская",
            "Самарская",
            "Саратовская",
            "Сахалинская",
            "Свердловская",
            "Смоленская",
            "Тамбовская",
            "Тверская",
            "Томская",
            "Тульская",
            "Тюменская",
            "Ульяновская",
            "Челябинская",
            "Ярославская",
            "край",
            "Алтайский",
            "Забайкальский",
            "Камчатский",
            "Краснодарский",
            "Красноярский",
            "Пермский",
            "Приморский",
            "Ставропольский",
            "Хабаровский",
            "республика",
            "Адыгея",
            "Алтай",
            "Башкортостан",
            "Бурятия",
            "Дагестан",
            "Ингушетия",
            "Кабардино-Балкария",
            "Калмыкия",
            "Карачаево-Черкесия",
            "Карелия",
            "Коми",
            "Марий Эл",
            "Мордовия",
            "Саха (Якутия)",
            "Северная Осетия-Алания",
            "Татарстан",
            "Тыва",
            "Удмуртия",
            "Хакасия",
            "Чечня",
            "Чувашия",
            "другое",
            "Еврейская АО",
            "Ненецкий АО",
            "Ханты-Мансийский АО",
            "Чукотский АО",
            "Ямало-Ненецкий АО",
            "другая страна",
            "Казахстан",
            "Белоруссия",
            "Украина"
    );
}
