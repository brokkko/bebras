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

        String eventId = Event.currentId();

        if (User.isAuthorized()) {
            menu.add(new MenuItem("Соревнование", routes.UserInfo.contestsList(eventId)));
            menu.add(new MenuItem("Личные данные", routes.UserInfo.info(eventId)));
            menu.add(new MenuItem("Выход", routes.Registration.logout(eventId)));
        } else {
            menu.add(new MenuItem("Вход", routes.Registration.login(eventId)));
            menu.add(new MenuItem("Регистрация", routes.Registration.registration(eventId)));
            menu.add(new MenuItem("Восстановление пароля", routes.Registration.passwordRemind(eventId)));
        }

        //TODO show only if this is defined in Event
//        menu.add(new MenuItem("Тренировочное соревнование", null));

        return menu;
    }

}