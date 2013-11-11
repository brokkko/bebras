package plugins.questionnaire;

import models.Contest;
import models.Event;
import models.User;
import models.forms.RawForm;
import models.newserialization.*;
import models.results.Info;
import models.results.InfoPattern;
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
    private boolean needAllFinished = true;

    private InfoPattern pattern;

    public InfoPattern getPattern() {
        return pattern;
    }

    @Override
    public void initPage() {
        if (!User.isAuthorized())
            return;
        User user = User.current();
        if (!user.hasRight(right))
            return;
        List<Contest> contests = Event.current().getContestsAvailableForUser(user);

        boolean oneFinished = false;
        boolean allFinished = true;
        for (Contest contest : contests)
            if (user.userParticipatedAndFinished(contest))
                oneFinished = true;
            else
                allFinished = false;

        boolean showMenu = needAllFinished && allFinished || !needAllFinished && oneFinished;
        if (showMenu)
            Menu.addMenuItem("Анкета участника", getCall(), right);
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
    public Result doGet(String action, String params) {
        if (!User.currentRole().hasRight(right))
            return Results.forbidden();

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

        return Results.ok(views.html.questionnaire.questions.render(blocks, filled, answers, getCall("go", false, "")));
    }

    @Override
    public Result doPost(String action, String params) {
        if (!User.currentRole().hasRight(right))
            return Results.forbidden();

        RawForm form = new RawForm();
        form.bindFromRequest();

        Info info = new Info();
        for (String key : getKeys()) {
            String value = form.get(key);
            info.put(key, value);
        }

        Info userValue = new Info("f", true, "ans", info);

        User user = User.current();
        user.getInfo().put(userField, userValue);
        user.store();

        return Results.redirect(getCall());
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);

        if (needAllFinished)
            serializer.write("finish all", needAllFinished);
        serializer.write("right", right);
        if (!"questionnaire".equals(userField))
            serializer.write("user field", userField);

        SerializationTypesRegistry.list(new QuestionBlockSerializationType()).write(serializer, "blocks", blocks);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        needAllFinished = deserializer.readBoolean("finish all", false);
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
