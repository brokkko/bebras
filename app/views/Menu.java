package views;

import controllers.routes;
import models.Event;
import models.User;
import models.UserRole;
import play.mvc.Call;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 09.01.13
 * Time: 23:30
 */
public class Menu {

    public static Menu current() {
        Map<String,Object> contextArgs = Http.Context.current().args;
        Menu menu = (Menu) contextArgs.get("menu");
        if (menu == null) {
            menu = new Menu();
            contextArgs.put("menu", menu);
        }

        return menu;
    }

    private List<MenuItem> items;

    public Menu() {
        List<MenuItem> menu = new ArrayList<>();

        Event event = Event.current();
        String eventId = event.getId();

        if (User.isAuthorized()) {
            menu.add(new MenuItem("Соревнование", routes.UserInfo.contestsList(eventId)));
            menu.add(new MenuItem("Личные данные", routes.UserInfo.info(eventId)));

            User user = User.current();
            UserRole role = user.getRole();
            if (role.mayRegisterSomebody())
                menu.add(new MenuItem("Регистрация", routes.Registration.registrationByUser(eventId)));

            if (user.hasEventAdminRight()) {
                menu.add(new MenuItem("Администрирование", routes.EventAdministration.admin(eventId)));
                menu.add(new MenuItem("Помощь", routes.EventAdministration.help(eventId)));
            }

            menu.add(new MenuItem("Выход", routes.Registration.logout(eventId)));
        } else {
            menu.add(new MenuItem("Вход", routes.Registration.login(eventId)));

            UserRole role = event.getAnonymousRole();
            if (role.mayRegisterSomebody())
                menu.add(new MenuItem("Регистрация", routes.Registration.registration(eventId)));

            menu.add(new MenuItem("Восстановление пароля", routes.Registration.passwordRemind(eventId)));
        }

        this.items = menu;
    }

    public void addMenuItem(String title, Call call) {
        int len = items.size();
        items.add(len - 1, new MenuItem(title, call)); //add before "Exit"
    }

    public List<MenuItem> items() {
        return items;
    }
}