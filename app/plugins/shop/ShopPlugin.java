/*
package plugins.shop;

import models.Event;
import models.User;
import models.applications.Application;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newserialization.*;
import models.shop.Item;
import models.shop.ItemWithCount;
import models.shop.Order;
import models.utils.Utils;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import plugins.applications.*;
import views.Menu;

import java.util.*;

import static play.mvc.Results.*;
import static play.mvc.Results.notFound;

public class ShopPlugin extends Plugin {
    private String right = "school org";
    private String userField = "shop_orders";
    private String menuTitle = "shop";
    private List<PaymentType> paymentTypes;
    private List<Item> items;

    @Override
    public void initPage() {
        Menu.addMenuItem(menuTitle, getOrdersCall(), right);
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(
                right,
                userField,
                SerializationTypesRegistry.list(new SerializableSerializationType<>(Order.class)),
                "Заказы"
        );
    }

    @Override
    public F.Promise<Result> doGet(String action, String params) {
        boolean level1 = User.currentRole().hasRight(right);

        switch (action) {
            case "orders":
                if (level1)
                    return F.Promise.pure(showOrders());
                break;
        }

        return F.Promise.pure(Results.notFound());
    }

    @Override
    public F.Promise<Result> doPost(String action, String params) {
        boolean level1 = User.currentRole().hasRight(right);

        switch (action) {
            case "remove_order":
                if (level1)
                    return F.Promise.pure(removeOrder(params));
                else
                    return F.Promise.pure(Results.forbidden());
            case "add_order":
                if (level1)
                    return F.Promise.pure(addOrder());
                else
                    return F.Promise.pure(Results.forbidden());
            case "do_payment":
                return F.Promise.pure(doPayment(params));
        }

        return F.Promise.pure(Controller.notFound());
    }

    private Result showOrders() {
        return showOrders(new RawForm());
    }

    private Result showOrders(RawForm addForm) {
        User user = User.current();

        if (user == null)
            return Results.badRequest("no user");

        List<Order> orders = getOrders(user);

        return ok(views.html.applications.shop_orders.render(Event.current(), orders, addForm, this));
    }

    private List<Order> getOrders(User user) {
        //noinspection unchecked
        return (List<Order>) user.getInfo().computeIfAbsent(userField, v -> new ArrayList<>());
    }

    private Result removeOrder(String id) {
        User user = User.current();

        if (user == null)
            return Results.badRequest("no user");

        List<Order> orders = getOrders(user);

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getId().toHexString().equals(id)) {
                orders.remove(i);
                user.store();

                return Results.redirect(getOrdersCall());
            }
        }

        return Results.notFound();
    }

    private Call getOrdersCall() {
        return getCall("orders");
    }

    private Result addOrder() {
        User user = User.current();

        if (user == null)
            return Results.badRequest();

        List<Order> orders = getOrders(user);

        InputForm addOrderForm = getAddOrderForm();
        FormDeserializer deserializer = new FormDeserializer(addOrderForm);
        RawForm rawForm = deserializer.getRawForm();
        if (rawForm.hasErrors())
            return ok(views.html.applications.shop_orders.render(Event.current(), orders, rawForm, this));

        Order newOrder = Order.createOrder();
        for (int i = 0; i < items.size(); i++) {
            String title = deserializer.readString("name" + i);
            if (orderTitle == null || orderTitle.isEmpty())
                break;
            int price = deserializer.readInt("price" + i);
            int count = deserializer.readInt("count" + i);

            Item item = searchItem(title, price);
            if (item == null)
                return badRequest(views.html.applications.shop_orders.render(Event.current(), orders, rawForm, this));

            newOrder.getItems().add(new ItemWithCount(item, count));
        }

        String type = deserializer.readString("type");
        int size = deserializer.readInt("size");

        addOrderForUser(user, event, size, appType);

        return Controller.redirect(getOrdersCall());
    }

    private Order addOrderForUser(User user, Event event, int size, ApplicationType appType) {
        List<Order> orders = getOrders(user);

        Order newOrder = Order.createOrder();

        orders.add(newOrder);
        user.store();

        return newOrder;
    }

    private Result doPayment(String userAndName) {
        String[] userAndNameSplit = userAndName.split("/");
        if (userAndNameSplit.length != 2)
            return badRequest();

        //get user
        String userId = userAndNameSplit[0];
        ObjectId userObjectId;
        try {
            userObjectId = new ObjectId(userId);
        } catch (IllegalArgumentException e) {
            return notFound("user not found");
        }
        User user = User.getUserById(userObjectId);
        if (user == null)
            return notFound("user not found");

        //get app name
        String applicationName = userAndNameSplit[1];

        RawForm form = new RawForm();
        form.bindFromRequest();
        String comment = form.get("comment");

        Application application = getApplicationByName(applicationName, user);
        if (application == null)
            return Controller.notFound("user not found");

        String type = application.getType();
        ApplicationType applicationType = getTypeByName(type);
        if (applicationType == null)
            return Controller.notFound("app not found");

        if (!User.currentRole().hasRight(applicationType.getRightToPay()))
            return Controller.forbidden();

        Result result = Controller.redirect(getViewAppCall(user, applicationName));

        doPayment(user, application, comment);

        return result;
    }

    public void doPayment(User user, Application application, String comment) {
        if (application.getState() == Application.NEW) {
            application.setState(Application.PAYED);
            application.setComment(comment);
            user.store();
        }
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);

        serializer.write("user field", userField);
        serializer.write("right", right);
        serializer.write("menu", menuTitle);

        SerializationTypesRegistry.list(new SerializableSerializationType<>(Item.class)).write(serializer, "items", items);
        SerializationTypesRegistry.list(new PaymentTypeSerializationType()).write(serializer, "payment types", paymentTypes);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        userField = deserializer.readString("user field", "apps");
        right = deserializer.readString("right", "school org");
        menuTitle = deserializer.readString("menu", "Заявки");

        items = SerializationTypesRegistry.list(new SerializableSerializationType<>(Item.class)).read(deserializer, "items");
        paymentTypes = SerializationTypesRegistry.list(new PaymentTypeSerializationType()).read(deserializer, "payment types");
    }

    private InputForm getAddOrderForm() {
        Map<String, Object> formMap = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        formMap.put("fields", fields);
        formMap.put("validators", Collections.emptyList());

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> itemName = Utils.mapify(
                    "name", "name" + i,
                    "view", Utils.mapify(
                            "type", "string",
                            "title", "Название"
                    ),
                    "required", true,
                    "validators", Collections.emptyList()
            );
            Map<String, Object> itemPrice = Utils.mapify(
                    "name", "price" + i,
                    "view", Utils.mapify(
                            "type", "int",
                            "title", "Цена"
                    ),
                    "required", true,
                    "validators", Collections.emptyList()
            );
            Map<String, Object> itemCount = Utils.mapify(
                    "name", "count" + i,
                    "view", Utils.mapify(
                            "type", "int",
                            "title", "Количество"
                    ),
                    "required", true,
                    "validators", Utils.listify(
                            Utils.mapify(
                                    "type", "int",
                                    "compare", ">0"
                            )
                    )
            );
            fields.add(itemName);
            fields.add(itemPrice);
            fields.add(itemCount);
        }

        fields.add(Utils.mapify(
                "name", "comments",
                "view", null,
                "required", false,
                "validators", Utils.listify()
        ));

        return InputForm.deserialize(new MemoryDeserializer(formMap));
    }
}
*/
