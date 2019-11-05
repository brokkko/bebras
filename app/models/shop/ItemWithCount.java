/*
package models.shop;

import models.newserialization.*;

public class ItemWithCount implements SerializableUpdatable {

    private Item item;
    private int count;

    public ItemWithCount() {
    }

    public ItemWithCount(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("item", item);
        serializer.write("count", count);
    }

    @Override
    public void update(Deserializer deserializer) {
        item = new SerializableSerializationType<>(Item.class).read(deserializer, "item");
        count = deserializer.readInt("count", 0);
    }
}
*/
