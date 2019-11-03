package models.shop;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;

import java.util.Objects;

public class Item implements SerializableUpdatable {

    private String name;
    private int price;

    public Item() {
    }

    public Item(String name, int price) {
        this.name = name;
        this.price = price;
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
