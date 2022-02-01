package plugins.bebrascard;

import models.Event;
import models.ServerConfiguration;
import models.User;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Result;
import plugins.Plugin;
import views.Menu;
import views.html.bebras_card;

import java.util.LinkedHashMap;
import java.util.Random;

import static play.mvc.Results.*;

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
537x376, 158 размер картинки, 30 сверху снизу и сбоку

find . -name "*_?.jpg" -print0 | xargs -0 -I {file} convert {file} -resize 235x235 {file}_resize.jpg

preview img: 537 x 376

db.users.findOne({event_id:"bebras16", login:"iposov"})['bebras-card-solved']
db.users.update({event_id:"bebras16", login:"iposov"}, {$set: {"bebras-card-solved": false}})
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
    private int year;
    private LinkedHashMap<String, String> extraCards; //maps link to name

    @Override
    public void initPage() {
        if (showInMenu)
            Menu.addMenuItem(title, getCall(), right, "_blank", 0);
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(right, CARD_ID_FIELD, new BasicSerializationType<>(String.class), "Bebras card id");
        event.registerExtraUserField(right, CARD_SOLVED_FIELD, new BasicSerializationType<>(Boolean.class), "Bebras card solved");
    }

    @Override
    public F.Promise<Result> doGet(String action, String params) {
        switch (action) {
            case "go":
                return F.Promise.pure(editCard());
            case "extra":
                return F.Promise.pure(extraCard(params));
//            case "view": //TODO think about viewing, make google not index these links, etc...
//                return viewCard(params);
//            case "preview":
//                return previewCard();
            default:
                return F.Promise.pure(notFound());
        }
    }

    @Override
    public F.Promise<Result> doPost(String action, String params) {
        switch (action) {
            case "win":
                return F.Promise.pure(doWin(params));
            default:
                return F.Promise.pure(notFound());
        }
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);

        serializer.write("right", right);
        serializer.write("title", title);
        if (!showInMenu)
            serializer.write("menu", false);
        serializer.write("year", year);

        SerializationTypesRegistry.map(String.class).write(serializer, "extra cards", extraCards);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        right = deserializer.readString("right");
        title = deserializer.readString("title");
        showInMenu = deserializer.readBoolean("menu", true);
        year = deserializer.readInt("year", 2016);

        extraCards = SerializationTypesRegistry.map(String.class).read(deserializer, "extra cards");
    }

    private Random getRandom(User user) {
        String id = cardId(user);
        return new Random(id.hashCode());
    }

    private Result editCard() {
        User user = User.current();

        if (user == null)
            return forbidden();

        if (!user.hasRight(right))
            return forbidden();

        Random rnd = getRandom(user);

        BebrasCard bc = new BebrasCard(CountriesData.get(), rnd);

        Call winCall = getCall("win", false, "");
        Call viewCall = getCall("view", true, cardId(user));

        String name = user.getFullName();

        if (solvedCard(user)) { //renderer considers card solved if win url is null
            winCall = null;
            bc.solve();
        }

        return ok(bebras_card.render("Bebras cards", BIG_WIDTH, BIG_HEIGHT, BIG_IMG_SIZE, year, bc, name, winCall, viewCall));
    }

    private Result extraCard(String params) {
        String name = extraCards.get(params);
        if (name == null)
            return notFound();

        Random rnd = new Random(params.hashCode());

        BebrasCard bc = new BebrasCard(CountriesData.get(), rnd);

        Call winCall = getCall("win", false, "");

        return ok(bebras_card.render("Bebras cards", BIG_WIDTH, BIG_HEIGHT, BIG_IMG_SIZE, year, bc, name, winCall, null));
    }

    private Result doWin(String info) {
        User user = User.current();

        if (user == null)
            return forbidden();

        if (!user.hasRight(right))
            return forbidden();

        Random rnd = getRandom(user);

        BebrasCard bc = new BebrasCard(CountriesData.get(), rnd);

        if (!info.matches("[0-4]{6}"))
            return badRequest();

        CountryData cd = null;
        for (int i = 0; i < 6; i++) {
            int ind = info.charAt(i) - '0';
            BebrasCardSlot slot = bc.getSlot(i);
            CountryData countryData = slot.getCountries().get(ind);
            if (cd == null)
                cd = countryData;
            else if (cd != countryData)
                return badRequest();
        }

        user.getInfo().put(CARD_SOLVED_FIELD, true);
        user.store();

        return ok();
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

//    private Result previewCard() {
//        return null;
//    }

    private Result viewCard(String cardId) {
        User user = User.getInstance(CARD_ID_FIELD, cardId);

        if (user == null)
            return notFound();

        if (!solvedCard(user))
            return forbidden();

        Random rnd = getRandom(user);

        BebrasCard bc = new BebrasCard(CountriesData.get(), rnd);
        bc.solve();

        Call viewCall = getCall("view", true, cardId);

        String name = user.getFullName();

        return ok(bebras_card.render("Bebras cards", BIG_WIDTH, BIG_HEIGHT, BIG_IMG_SIZE, year, bc, name, null, viewCall));
    }
}
