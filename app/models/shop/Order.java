/*
package models.shop;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Order implements SerializableUpdatable {

    public static Order createOrder() {
        Order newOrder = new Order();

        newOrder.id = new ObjectId();
        newOrder.state = OrderState.CREATED;
        newOrder.comment = "";
        newOrder.items = new ArrayList<>();

        return newOrder;
    }

    private ObjectId id;
    private OrderState state;
    private String comment;
    private List<ItemWithCount> items;

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("_id", id);
        serializer.write("state", state.toString());
        serializer.write("comment", comment);
        SerializationTypesRegistry.list(Item.class).write(serializer, "items", items);
    }

    @Override
    public void update(Deserializer deserializer) {
        id = deserializer.readObjectId("_id");
        state = OrderState.valueOf(deserializer.readString("state", OrderState.CREATED.toString()));
        comment = deserializer.readString("comment");
        items = SerializationTypesRegistry.list(ItemWithCount.class).read(deserializer, "items");
    }

    public ObjectId getId() {
        return id;
    }

    public OrderState getState() {
        return state;
    }

    public String getComment() {
        return comment;
    }

    public List<ItemWithCount> getItems() {
        return items;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
*/
