package views;

import controllers.routes;
import models.Event;
import models.User;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.01.13
 * Time: 23:30
 */
public class Menu {

    public static List<MenuItem> current() {
        List<MenuItem> menu = new ArrayList<>();

        if (User.isAuthorized()) {
            menu.add(new MenuItem("Личные данные", null));
            menu.add(new MenuItem("Соревнование", null));
        } else {
            menu.add(new MenuItem("Вход", null));
            menu.add(new MenuItem("Регистрация", routes.Registration.registration(Event.current())));
            menu.add(new MenuItem("Восстановление пароля", null));
        }

        menu.add(new MenuItem("Тренировочное соревнование", null));

        return menu;
    }

}