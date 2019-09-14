package models.shop;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;

public class Item implements SerializableUpdatable {

    private String name;
    private int price;

    public Item() {
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("name", name);
        serializer.write("price", price);
    }

    @Override
    public void update(Deserializer deserializer) {
        name = deserializer.readString("name");
        price = deserializer.readInt("price");
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
