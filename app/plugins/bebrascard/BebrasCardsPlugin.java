package plugins.bebrascard;

import models.Event;
import models.ServerConfiguration;
import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import views.Menu;
import views.html.bebras_card;

import java.util.Random;

import static play.mvc.Results.notFound;

/*
{
  "ref" : "bc",
  "right" : "event admin",
  "title" : "Открытки",
  "menu": true,
  "type": "bebras cards"
}

2480 496 992
1755 351 702


2480x1755, 728
800x560, 235 размер картинки
vk
537x376, 158 размер картинки

find . -name "*_?.jpg" -print0 | xargs -0 -I {file} convert {file} -resize 235x235 {file}_resize.jpg

preview img: 537 x 376
 */

public class BebrasCardsPlugin extends Plugin {

    public static final String PLUGIN_NAME = "BebrasCards";
    public static final int BIG_WIDTH = 800;
    public static final int BIG_HEIGHT = 800 * 7 / 10;
    public static final int BIG_IMG_SIZE = 235;
    public static final String CARD_ID_FIELD = "bebras-card-id";
    public static final String CARD_SOLVED_FIELD = "bebras-card-solved";

    private String right; //право на просмотр
    private String title; //текст на кнопке меню
    private boolean showInMenu; //показывать ли в меню

    @Override
    public void initPage() {
        if (showInMenu)
            Menu.addMenuItem(title, getCall(), right);
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(right, CARD_ID_FIELD, new BasicSerializationType<>(String.class), "Bebras card id");
        event.registerExtraUserField(right, CARD_SOLVED_FIELD, new BasicSerializationType<>(Boolean.class), "Bebras card solved");
    }

    @Override
    public Result doGet(String action, String params) {
        switch (action) {
            case "go":
                return editCard();
            case "view":
                return viewCard();
            case "preview":
                return previewCard();
            default:
                return notFound();
        }
    }

    @Override
    public Result doPost(String action, String params) {
        return null;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);

        serializer.write("right", right);
        serializer.write("title", title);
        if (!showInMenu)
            serializer.write("menu", false);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        right = deserializer.readString("right");
        title = deserializer.readString("title");
        showInMenu = deserializer.readBoolean("menu", true);
    }

    private Random getRandom(User user) {
        String id = cardId(user);
        return new Random(id.hashCode());
    }

    private Result editCard() {
        User user = User.current();

        Random rnd = getRandom(user);

        BebrasCard bc = new BebrasCard(CountriesData.get(), rnd);

        return Results.ok(bebras_card.render("Bebras cards", BIG_WIDTH, BIG_HEIGHT, BIG_IMG_SIZE, bc));
    }

    private boolean solvedCard(User user) {
        Boolean solved = (Boolean) user.getInfo().get(CARD_SOLVED_FIELD);
        return solved != null && solved;
    }

    private String cardId(User user) {
        String id = (String) user.getInfo().get(CARD_ID_FIELD);
        if (id == null) {
            id = ServerConfiguration.getInstance().getRandomString(10);
            user.getInfo().put(CARD_ID_FIELD, id);
            user.store();
        }

        return id;
    }

    private Result previewCard() {
        return null;
    }

    private Result viewCard() {
        return null;
    }
}