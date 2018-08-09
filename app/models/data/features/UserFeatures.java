package models.data.features;

import models.Contest;
import models.User;
import models.data.FeaturesContext;
import models.data.FeaturesSet;
import models.data.WrappedFeatureValue;
import models.forms.RawForm;
import models.newserialization.FlatSerializer;
import org.bson.types.ObjectId;
import play.Logger;
import plugins.Plugin;
import plugins.upload.FileDescription;
import plugins.upload.FileInfo;
import plugins.upload.UploadPlugin;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 24.07.13
 * Time: 23:59
 */
public class UserFeatures implements FeaturesSet<User> {

    private User user;
    private RawForm rawForm;
    private ObjectId id;

    private Map<ObjectId, RawForm> user2form = new HashMap<>();

    @Override
    public void load(User user) throws Exception {
        rawForm = convertUserToForm(user);

        id = user.getId();

        this.user = user;
    }

    private RawForm convertUserToForm(User user) {
        FlatSerializer serializer = new FlatSerializer(".");
        user.serialize(serializer);
        return serializer.getRawForm();
    }

    @Override
    public Object getFeature(String featureName, FeaturesContext context) {
        if (rawForm == null)
            throw new IllegalStateException("Object not loaded");

        RawForm effectiveRawForm = rawForm;
        User effectiveUser = user;

        while (featureName.startsWith("~reg_by.")) {
            featureName = featureName.substring("~reg_by.".length());

            effectiveUser = effectiveUser.getRegisteredByUser();
            if (effectiveUser == null)
                return null;

            effectiveRawForm = user2form.get(effectiveUser.getId());
            if (effectiveRawForm == null) {
                effectiveRawForm = convertUserToForm(effectiveUser);
                user2form.put(effectiveUser.getId(), effectiveRawForm);
            }
        }

        if (featureName.startsWith("~contest#")) {
            List<Contest> contests = context.getEvent().getContestsAvailableForUser(effectiveUser);
            featureName = featureName.substring("~contest#".length());
            int dotPos = featureName.indexOf('.');

            int contestInd;
            String featureTail;
            try {
                if (dotPos < 0) {
                    contestInd = Integer.parseInt(featureName);
                    featureTail = "";
                } else {
                    contestInd = Integer.parseInt(featureName.substring(0, dotPos));
                    featureTail = featureName.substring(dotPos);
                }
            } catch (NumberFormatException e) {
                return null;
            }

            if (contestInd < 1 || contestInd > contests.size())
                return null;

            featureName = "_contests." + contests.get(contestInd - 1).getId() + featureTail;
        }

        if (featureName.startsWith("uploaded-file-name---")) {
            String info = featureName.substring("uploaded-file-name---".length());
            String[] refAndId = info.split("---");
            if (refAndId.length != 2)
                return "??";

            String ref = refAndId[0];
            String id = refAndId[1];

            Plugin plugin = context.getEvent().getPlugin(ref);
            if (!(plugin instanceof UploadPlugin))
                return "??";
            UploadPlugin uploadPlugin = (UploadPlugin) plugin;

            FileDescription fd = uploadPlugin.searchFileDescription(id);
            if (fd == null)
                return "??";

            FileInfo fi = new FileInfo(context.getEvent(), user, fd);
            return fi.isUploaded() ? fi.getFile().getName() : "not uploaded";
        }

        if (featureName.startsWith("~")) {
            switch (featureName) {
                case "~oid_inc":
                    return Long.toHexString(id.getCounter()).toUpperCase();
                case "~oid_time":
                    return Long.toHexString(id.getTimestamp()).toUpperCase();
                case "~oid_machine":
                    return Long.toHexString(id.getMachineIdentifier()).toUpperCase();
                case "~sex":
                    return determineSex(effectiveRawForm);
                default:
                    return "";
            }
        }

        return effectiveRawForm.get(featureName);
    }

    private String determineSex(RawForm form) {
        String name = form.get("name");
        String surname = form.get("surname");
        String patronymic = form.get("patronymic");

        if (patronymic != null)
            return patronymic.endsWith("а") ? "F" : patronymic.endsWith("ч") ? "M" : "?";

        if (name == null)
            return "?";

        name = name.toLowerCase().trim().replaceAll("ё", "е");

        if (MALE_NAMES.contains(name))
            return "M";
        if (FEMALE_NAMES.contains(name))
            return "F";

        if (surname == null)
            return "?";

        surname = surname.toLowerCase().trim().replaceAll("ё", "е");

        for (String vowel : new String[]{"а", "о", "у", "э", "ы", "я", "ё", "ю", "е", "и"})
            if (surname.endsWith(vowel))
                return "F?";

        return "M?";
    }

    @Override
    public void close() throws Exception {
        user = null;
        rawForm = null;
    }

    public static final List<String> MALE_NAMES_LIST = Arrays.asList(
            "иван",
            "павел",
            "данил",
            "даниил",
            "андрей",
            "владимир",
            "владислав",
            "максим",
            "карим",
            "данияр",
            "алексей",
            "федор",
            "игорь",
            "миша",
            "арслан",
            "дмитрий",
            "кирилл",
            "богдан",
            "александр",
            "егор",
            "артем",
            "миша",
            "георгий",
            "ярослав",
            "митя",
            "ильдар",
            "григорий",
            "никита",
            "юрий",
            "михаил",
            "роман",
            "матвей",
            "юрий",
            "николай",
            "эдуард",
            "рауль",
            "арсентий",
            "леонид",
            "сергей",
            "иннокентий",
            "марк",
            "семен",
            "вадим",
            "арафат",
            "тамерлан",
            "алим",
            "мухамед",
            "азамат",
            "ислам",
            "адислан",
            "мартин",
            "аминат",
            "яков",
            "денис",
            "никон",
            "алеша",
            "рамазан",
            "шахман",
            "арсений",
            "руслан",
            "рома",
            "илья",
            "ваня",
            "влад",
            "генрих",
            "камиль",
            "тимур",
            "константин",
            "данила",
            "армавир",
            "антон",
            "константин",
            "рустам",
            "исмаил",
            "илнар",
            "прохор",
            "виталий",
            "валерий",
            "глеб",
            "оскар",
            "евгений",
            "марат",
            "борис",
            "давид",
            "дамир",
            "виктор",
            "юсуп",
            "арсен",
            "артемий",
            "петр",
            "анатолий",
            "артур",
            "василий",
            "ираклий",
            "валентин",
            "степан",
            "алексанлр",
            "алексанр",
            "юра",
            "гриша",
            "олег",
            "наполеон",
            "лев",
            "тигран",
            "жора",
            "слава",
            "костя",
            "макарий",
            "серафим",
            "феликс",
            "сева",
            "назар",
            "тарас",
            "ян",
            "всеволод",
            "эльвин",
            "никалай",
            "алижон",
            "дилшод",
            "джавохир",
            "эмиль",
            "эрик",
            "милан",
            "роберт",
            "тимофей",
            "святослав",
            "дагниил",
            "вячеслав",
            "стас",
            "ростислав",
            "клим",
            "игнат",
            "арман",
            "эльмар",
            "осман",
            "герман",
            "гасан",
            "гор",
            "гарик",
            "захар",
            "шукри",
            "киилл",
            "ахмед",
            "нарек",
            "нурлан",
            "дима",
            "алмаз",
            "абаз",
            "тельман",
            "станислав",
            "енисей",
            "вова",
            "родион",
            "вазген",
            "сережа",
            "муслим",
            "ренат",
            "анар",
            "коля",
            "геннадий",
            "валера",
            "леон",
            "дэвид",
            "даур",
            "мирослав",
            "жахонгир",
            "жаргал",
            "савелий",
            "батор",
            "алдар",
            "мэргэн",
            "эрдэм",
            "зорик",
            "золто",
            "баир",
            "булат",
            "эрдэни",
            "виталя",
            "анатолй",
            "магомед",
            "мурад",
            "ринат",
            "гордей",
            "доджи",
            "ильяс",
            "корюн",
            "платон",
            "влдислав",
            "самандар",
            "керим",
            "ходжиакбар",
            "юриан",
            "аркадий",
            "иззат",
            "миргияс",
            "булат",
            "фаррух",
            "алексадр",
            "рубен",
            "савелий",
            "вениамин",
            "алий",
            "эльдар",
            "бислан",
            "владилен",
            "ильяс",
            "фархад",
            "вася",
            "паша",
            "михаил",
            "родион",
            "никита",
            "юрий",
            "ратибор",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    );

    public static final List<String> FEMALE_NAMES_LIST = Arrays.asList(
            "мари",
            "таисия",
            "ани",
            "владислава",
            "евгения",
            "валерия",
            "светлана",
            "елена",
            "камилла",
            "юлия",
            "ольга",
            "ева",
            "дарья",
            "берта",
            "ева",
            "геля",
            "ксения",
            "татьяна",
            "арина",
            "алина",
            "алена",
            "яна",
            "мария",
            "марина",
            "маргарита",
            "екатерина",
            "любовь",
            "елизавета",
            "полина",
            "виктория",
            "александра",
            "софья",
            "анастасия",
            "настя",
            "диана",
            "кристина",
            "катя",
            "анна",
            "даша",
            "вероника",
            "инесса",
            "зарема",
            "элона",
            "дарина",
            "аида",
            "залина",
            "аляна",
            "оксана",
            "ирина",
            "джулия",
            "надежда",
            "наталья",
            "кира",
            "варвара",
            "людмила",
            "аня",
            "дрья",
            "софия",
            "милена",
            "сабина",
            "эльвира",
            "виолетта",
            "генриетта",
            "алиса",
            "карина",
            "олеся",
            "надя",
            "ангелина",
            "дария",
            "вика",
            "снежана",
            "эля",
            "ксюша",
            "василина",
            "галина",
            "рената",
            "фатимат",
            "валентина",
            "анжелика",
            "майя",
            "наталия",
            "маша",
            "вера",
            "антонина",
            "варя",
            "тамара",
            "ульяна",
            "анфиса",
            "злата",
            "алла",
            "эжени",
            "анжела",
            "влада",
            "катерина",
            "лиза",
            "рита",
            "эвелина",
            "лика",
            "селина",
            "лада",
            "гульнара",
            "ярослава",
            "глафира",
            "эмилия",
            "люба",
            "алсу",
            "виталина",
            "доминика",
            "жанна",
            "бронислава",
            "наташа",
            "маргаритта",
            "милана",
            "эльмра",
            "лилия",
            "алеся",
            "динара",
            "елизовета",
            "инга",
            "александрина",
            "варавара",
            "амалия",
            "ирада",
            "оля",
            "элина",
            "амина",
            "виола",
            "каролина",
            "айтен",
            "дилфуза",
            "патимат",
            "айсун",
            "турсун",
            "жаля",
            "зухра",
            "инна",
            "мадима",
            "жансулу",
            "алевтина",
            "рада",
            "ариадна",
            "альбина",
            "гузель",
            "таня",
            "лейла",
            "даяна",
            "лариса",
            "сюзанна",
            "ника",
            "нара",
            "айна",
            "эльвина",
            "ираида",
            "лианна",
            "юля",
            "лена",
            "лина",
            "анна",
            "евангелина",
            "елизавета",
            "дарья",
            "екатерина",
            "софия",
            "анастасия",
            "полина",
            "лера",
            "степанида",
            "юлианна",
            "васелина",
            "свелана",
            "",
            "",
            "",
            "",
            ""
    );

    public static final Set<String> MALE_NAMES = new HashSet<>(MALE_NAMES_LIST);
    public static final Set<String> FEMALE_NAMES = new HashSet<>(FEMALE_NAMES_LIST);
}
