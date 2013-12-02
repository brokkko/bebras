package views;

import controllers.routes;
import models.Event;
import models.User;
import models.UserRole;
import models.data.TableDescription;
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

    // if right is null, than allow for everybody even for anonymous
    public static void addMenuItem(String title, Call call, String right) {
        List<RestrictedAccessMenuItem> extraItems = getExtraItems();

        extraItems.add(new RestrictedAccessMenuItem(new MenuItem(title, call), right));
    }

    public List<MenuItem> items() {
        return items;
    }

    private List<MenuItem> items = new ArrayList<>();

    private Menu() {
        List<MenuItem> menu = new ArrayList<>();

        Event event = Event.current();
        String eventId = event == Event.ERROR_EVENT ? null : event.getId();

        if (eventId == null) {
            menu.add(new MenuItem("Список событий", routes.Application.listEvents()));
        } else if (User.isAuthorized()) {
            User user = User.current();

            if (user.isPartialRegistration()) {
                addPersonalDataMenuItem(menu, eventId);
                addExitMenuItem(menu, eventId);
            } else
                fillMenuForAuthorizedUser(menu, event, eventId, user);
        } else
            fillMenuForAnon(menu, event, eventId);

        items = menu;
    }

    private void fillMenuForAnon(List<MenuItem> menu, Event event, String eventId) {
        menu.add(new MenuItem("Вход", routes.Registration.login(eventId)));

        UserRole role = event.getAnonymousRole();
        if (role.mayRegisterSomebody())
            menu.add(new MenuItem("Регистрация", routes.Registration.registration(eventId)));

        menu.add(new MenuItem("Восстановление пароля", routes.Registration.passwordRemind(eventId)));

        fillExtraItems(menu);
    }

    private void fillMenuForAuthorizedUser(List<MenuItem> menu, Event event, String eventId, User user) {
        if (user.hasEventAdminRight())
            menu.add(new MenuItem("Событие", routes.EventAdministration.admin(eventId)));

        int contestsCount = event.getContestsAvailableForUser().size();
        if (contestsCount > 0 || User.current().hasEventAdminRight())
            menu.add(new MenuItem(contestsCount == 1 ? "Соревнование" : "Соревнования", routes.UserInfo.contestsList(eventId)));

        addPersonalDataMenuItem(menu, eventId);

        UserRole role = user.getRole();
        if (role.mayRegisterSomebody())
            menu.add(new MenuItem("Регистрация", routes.Registration.registrationByUser(eventId)));

        List<TableDescription<?>> tables = user.getTables();
        String tablesTitle = user.getRole().getTablesMenuTitle();
        if (tables.size() == 1) {
            if (tablesTitle == null)
                tablesTitle = tables.get(0).getTitle();
            menu.add(new MenuItem(tablesTitle, routes.Tables.showTable(eventId, 0)));
        } else if (tables.size() > 1) {
            if (tablesTitle == null)
                tablesTitle = "Данные";
            menu.add(new MenuItem(tablesTitle, routes.Tables.tablesList(eventId)));
        }

        if (user.hasEventAdminRight()) {
            menu.add(new MenuItem("Задания", routes.Problems.viewFolder(eventId, eventId)));
            menu.add(new MenuItem("Рассылка сообщений", routes.Announcements.prepareAnnouncement(eventId)));
        }

        fillExtraItems(menu);

        addExitMenuItem(menu, eventId);
    }

    private boolean addPersonalDataMenuItem(List<MenuItem> menu, String eventId) {
        return menu.add(new MenuItem("Личные данные", routes.UserInfo.info(eventId, null)));
    }

    private void addExitMenuItem(List<MenuItem> menu, String eventId) {
        if (User.getSubstitutedUser() != null) {
            String title = "Вы работаете от имени " + User.current().getLogin() + ". ";
            title += "Вернуться к " + User.getSubstitutedUser();
            menu.add(new MenuItem(title, routes.Application.substituteUserExit(Event.currentId())));
        } else
            menu.add(new MenuItem("Выход", routes.Registration.logout(eventId)));
    }

    private static List<RestrictedAccessMenuItem> getExtraItems() {
        Map<String,Object> contextArgs = Http.Context.current().args;
        //noinspection unchecked
        List<RestrictedAccessMenuItem> extraItems = (List<RestrictedAccessMenuItem>) contextArgs.get("menu-items");
        if (extraItems == null) {
            extraItems = new ArrayList<>();
            contextArgs.put("menu-items", extraItems);
        }
        return extraItems;
    }

    private void fillExtraItems(List<MenuItem> menu) {
        List<RestrictedAccessMenuItem> extraItems = getExtraItems();

        for (RestrictedAccessMenuItem extraItem : extraItems) {
            String right = extraItem.getRight();
            UserRole role = User.currentRole();
            if ("anon".equals(right) || role.hasRight(right)) //TODO remove anon role
                menu.add(extraItem.getItem());
        }
    }

    private static class RestrictedAccessMenuItem {
        private MenuItem item;
        private String right;

        private RestrictedAccessMenuItem(MenuItem item, String right) {
            this.item = item;
            this.right = right;
        }

        private MenuItem getItem() {
            return item;
        }

        private String getRight() {
            return right;
        }
    }
}