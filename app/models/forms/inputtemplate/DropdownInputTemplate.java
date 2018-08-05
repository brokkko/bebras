package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newserialization.*;
import play.twirl.api.Html;

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
        return views.html.fields.dropdown.render(form, field, placeholder, extraVariants, extraTitles, hint);
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

        if (load.startsWith("file://"))
            loadFromFile(load.substring("file://".length()));
        else
            loadBuiltInList(load);
    }

    private void loadFromFile(String fileName) {
        //TODO implement
    }

    private void loadBuiltInList(String load) {
        switch (load) {
            case "regions":
                extraVariants.addAll(REGION_VARIANTS);
                extraTitles.addAll(REGION_TITLES);
            case "by-regions":
                extraVariants.addAll(BY_REGION_VARIANTS);
                extraTitles.addAll(BY_REGION_TITLES);
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

    private static List<String> BY_REGION_VARIANTS = Arrays.asList(
            "~group",
            "MNS1",
            "MNS2",
            "MNS3",
            "MNS4",
            "MNS5",
            "MNS6",
            "MNS7",
            "MNS8",
            "MNS9",
            "~group",
            "BRS1",
            "BRS2",
            "BRS3",
            "BRS4",
            "BRS5",
            "BRS6",
            "BRS7",
            "BRS8",
            "BRS9",
            "BRS10",
            "BRS11",
            "BRS12",
            "BRS13",
            "BRS14",
            "BRS15",
            "BRS16",
            "BRS17",
            "BRS18",
            "BRS19",
            "BRS20",
            "~group",
            "VIT1",
            "VIT2",
            "VIT3",
            "VIT4",
            "VIT5",
            "VIT6",
            "VIT7",
            "VIT8",
            "VIT9",
            "VIT10",
            "VIT11",
            "VIT12",
            "VIT13",
            "VIT14",
            "VIT15",
            "VIT16",
            "VIT17",
            "VIT18",
            "VIT19",
            "VIT20",
            "VIT21",
            "VIT22",
            "VIT23",
            "VIT24",
            "VIT25",
            "~group",
            "GOM1",
            "GOM2",
            "GOM3",
            "GOM4",
            "GOM5",
            "GOM6",
            "GOM7",
            "GOM8",
            "GOM9",
            "GOM10",
            "GOM11",
            "GOM12",
            "GOM13",
            "GOM14",
            "GOM15",
            "GOM16",
            "GOM17",
            "GOM18",
            "GOM19",
            "GOM20",
            "GOM21",
            "GOM22",
            "GOM23",
            "GOM24",
            "GOM25",
            "GOM26",
            "~group",
            "GRD1",
            "GRD2",
            "GRD3",
            "GRD4",
            "GRD5",
            "GRD6",
            "GRD7",
            "GRD8",
            "GRD9",
            "GRD10",
            "GRD11",
            "GRD12",
            "GRD13",
            "GRD14",
            "GRD15",
            "GRD16",
            "GRD17",
            "GRD18",
            "GRD19",
            "~group",
            "MNO1",
            "MNO2",
            "MNO3",
            "MNO4",
            "MNO5",
            "MNO6",
            "MNO7",
            "MNO8",
            "MNO9",
            "MNO10",
            "MNO11",
            "MNO12",
            "MNO13",
            "MNO14",
            "MNO15",
            "MNO16",
            "MNO17",
            "MNO18",
            "MNO19",
            "MNO20",
            "MNO21",
            "MNO22",
            "MNO23",
            "~group",
            "MOG1",
            "MOG2",
            "MOG3",
            "MOG4",
            "MOG5",
            "MOG6",
            "MOG7",
            "MOG8",
            "MOG9",
            "MOG10",
            "MOG11",
            "MOG12",
            "MOG13",
            "MOG14",
            "MOG15",
            "MOG16",
            "MOG17",
            "MOG18",
            "MOG19",
            "MOG20",
            "MOG21",
            "MOG22",
            "MOG23",
            "MOG24",
            "MOG25",
            "MOG26",
            "MOG27"
    );

    private static List<String> BY_REGION_TITLES = Arrays.asList(
            "г. Минск",
            "Советский р-н",
            "Первомайский р-н",
            "Центральный р-н",
            "Фрунзенский р-н",
            "Ленинский р-н",
            "Заводской р-н",
            "Партизанский р-н",
            "Октябрьский р-н",
            "Московский р-н",
            "Брестская область",
            "г. Барановичи",
            "г. Пинск",
            "г. Брест, Ленинский р-н",
            "г. Брест, Московский р-н",
            "Брестский р-н",
            "Барановичский р-н",
            "Березовский р-н",
            "Ганцевичский р-н",
            "Дрогичинский р-н",
            "Жабинковский р-н",
            "Ивановский р-н",
            "Ивацевичский р-н",
            "Каменецкий р-н",
            "Кобринский р-н",
            "Лунинецкий р-н",
            "Ляховичский р-н",
            "Малоритский р-н",
            "Пинский р-н",
            "Пружанский р-н",
            "Столинский р-н",
            "Витебская область",
            "Бешенковичский р-н",
            "Браславский р-н",
            "Верхнедвинский р-н",
            "Витебский р-н",
            "Глубокский р-н",
            "Городокский р-н",
            "Докшицкий р-н",
            "Дубровенский р-н",
            "Лепельский р-н",
            "Лиозненский р-н",
            "Миорский р-н",
            "Оршанский р-н",
            "Полоцкий р-н",
            "Поставский р-н",
            "Россонский р-н",
            "Сенненский р-н",
            "Толочинский р-н",
            "Ушачский р-н",
            "Чашникский р-н",
            "Шарковщинский р-н",
            "Шумилинский р-н",
            "г. Новополоцк",
            "Октябрьский р-н р-н, г. Витебска",
            "Первомайский р-н р-н, г. Витебска",
            "Железнодорожный р-н, г. Витебска",
            "Гомельская область",
            "Брагинский р-н",
            "Буда-Кошелевский р-н",
            "Ветковский р-н",
            "Гомельский р-н",
            "Добрушский р-н",
            "Ельский р-н",
            "Житковичский р-н",
            "Жлобинский р-н",
            "Калинковичский р-н",
            "Кормянский р-н",
            "Лельчицкий р-н",
            "Лоевский р-н",
            "Мозырский р-н",
            "Наровлянский р-н",
            "Октябрьский р-н",
            "Петриковский р-н",
            "Речицкий р-н",
            "Рогачевский р-н",
            "Светлогорский р-н",
            "Хойникский р-н",
            "Чечерский р-н",
            "Центральный",
            "Железнодорожный",
            "Новобелицкий р-н",
            "Советский р-н",
            "г. Гомель",
            "Гродненская область",
            "Берестовицкий р-н",
            "Волковысский р-н",
            "Вороновский р-н",
            "Гродненский р-н",
            "Дятловский р-н",
            "Зельвенский р-н",
            "Ивьевский р-н",
            "Кореличский р-н",
            "Лидский р-н",
            "Мостовский р-н",
            "Новогрудский р-н",
            "Островецкий р-н",
            "Ошмянский р-н",
            "Свислочский р-н",
            "Слонимский р-н",
            "Сморгонский р-н",
            "Щучинский р-н",
            "Октябрьский р-н",
            "Ленинский р-н",
            "Минская область",
            "Березинский р-н",
            "Борисовский р-н",
            "Вилейский р-н",
            "Воложинский р-н",
            "Дзержинский р-н",
            "Клецкий р-н",
            "Копыльский р-н",
            "Крупский р-н",
            "Логойский р-н",
            "Любанский р-н",
            "Минский р-н",
            "Молодечненский р-н",
            "Мядельский р-н",
            "Несвижский р-н",
            "Пуховичский р-н",
            "Слуцкий р-н",
            "Смолевичский р-н",
            "Солигорский р-н",
            "Стародорожский р-н",
            "Столбцовский р-н",
            "Узденский р-н",
            "Червенский р-н",
            "г. Жодино",
            "Могилевская область",
            "Белыничский р-н",
            "Бобруйский р-н",
            "Быховский р-н",
            "Глусский р-н",
            "Горецкий р-н",
            "Дрибинский р-н",
            "Кировский р-н",
            "Климовичский р-н",
            "Кличевский р-н",
            "Костюковичский р-н",
            "Краснопольский р-н",
            "Кричевский р-н",
            "Круглянский р-н",
            "Могилевский р-н",
            "Мстиславский р-н",
            "Осиповичский р-н",
            "Славгородский р-н",
            "Хотимский р-н",
            "Чаусский р-н",
            "Чериковский р-н",
            "Шкловский р-н",
            "г. Могилев",
            "Ленинский р-н, Могилев",
            "Ленинский р-н, Бобруйск",
            "Октябрьский р-н, Могилев",
            "Первомайский р-н, Бобруйск",
            "г. Бобруйск"
    );
}