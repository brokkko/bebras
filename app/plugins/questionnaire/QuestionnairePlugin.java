package plugins.questionnaire;

import models.Contest;
import models.Event;
import models.User;
import models.forms.RawForm;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import models.results.Info;
import models.results.InfoPattern;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import views.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 11.11.13
 * Time: 16:02
 */
public class QuestionnairePlugin extends Plugin {

    private List<QuestionBlock> blocks = new ArrayList<>();
    private String right;
    private String userField;
    private int showRegime = 1; //0 means may show always, 1 means need at least 1 contest, 2 means need all contests
    private String titleBefore;
    private String titleAfter;

    private InfoPattern pattern;

    public InfoPattern getPattern() {
        return pattern;
    }

    @Override
    public void initPage() {
        if (!User.isAuthorized())
            return;
        User user = User.current();

        boolean showInMenu = user.hasEventAdminRight() || availableForUser(user);
        if (showInMenu)
            Menu.addMenuItem("Анкета участника", getCall(), right);
    }

    public boolean availableForUser(User user) {
        if (!user.hasRight(right))
            return false;

        List<Contest> contests = Event.current().getContestsAvailableForUser(user);
        //filter contests that are available for anon

        boolean oneFinished = false;
        boolean allFinished = true;
        int count = 0;
        for (Contest contest : contests) {
            if (contest.isAvailableForAnon())
                continue;

            count++;

            if (user.userParticipatedAndFinished(contest))
                oneFinished = true;
            else
                allFinished = false;
        }

        if (count == 0)
            allFinished = false;

        return (showRegime == 2) && allFinished || (showRegime == 1) && oneFinished || showRegime == 0;
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(
                right,
                userField, //TODO take this field from plugin configuration
                pattern,
                "Опросник"
        );
    }

    @Override
    public F.Promise<Result> doGet(String action, String params) {
        if (!User.currentRole().hasRight(right))
            return F.Promise.pure(Results.forbidden());

        User user = User.current();
        Info info = (Info) user.getInfo().get(userField);

        if (info == null)
            info = new Info("f", false, "ans", null);

        Boolean filled = (Boolean) info.get("f");
        if (filled == null)
            filled = false;

        Info answers = (Info) info.get("ans");
        if (answers == null)
            answers = new Info();

        return F.Promise.pure(Results.ok(views.html.questionnaire.questions.render(titleBefore, titleAfter, blocks, filled, answers, getCall("go", false, ""))));
    }

    @Override
    public F.Promise<Result> doPost(String action, String params) {
        if (!User.currentRole().hasRight(right))
            return F.Promise.pure(Results.forbidden());

        RawForm form = new RawForm();
        form.bindFromRequest();

        Info info = new Info();
        boolean allEmpty = true;
        for (String key : getKeys()) {
            String value = form.get(key);
            info.put(key, value);

            if (value != null && !value.equals(""))
                allEmpty = false;
        }

        Info userValue = new Info("f", !allEmpty, "ans", info);

        User user = User.current();
        user.getInfo().put(userField, userValue);
        user.store();

        return F.Promise.pure(Results.redirect(getCall()));
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);

        if (showRegime != 1)
            serializer.write("show regime", showRegime);
        serializer.write("right", right);
        serializer.write("title before", titleBefore);
        serializer.write("title after", titleAfter);
        if (!"questionnaire".equals(userField))
            serializer.write("user field", userField);

        SerializationTypesRegistry.list(new QuestionBlockSerializationType()).write(serializer, "blocks", blocks);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        showRegime = deserializer.readInt("show regime", 1);
        titleBefore = deserializer.readString("title before", "Заполните, пожалуйста, анкету");
        titleAfter = deserializer.readString("title after", "Спасибо, что заполнили анкету!");
        right = deserializer.readString("right", "questionnaire");
        userField = deserializer.readString("user field", "questionnaire");

        blocks = SerializationTypesRegistry.list(new QuestionBlockSerializationType()).read(deserializer, "blocks");

        initPatterns();
    }

    private void initPatterns() {
        InfoPattern answersPattern = new InfoPattern();
        for (String key : getKeys())
            answersPattern.register(key, new BasicSerializationType<>(String.class), key);
        pattern = new InfoPattern(
                "f", new BasicSerializationType<>(boolean.class), "filled",
                "ans", answersPattern, "answers"
        );
    }

    private List<String> getKeys() {
        List<String> result = new ArrayList<>();

        for (QuestionBlock block : blocks)
            if (block.getName() != null)
                result.add(block.getName());

        return result;
    }
}
