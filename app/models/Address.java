package models;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 18.01.13
 * Time: 19:27
 */
public class Address implements SerializableUpdatable {

    private String index;
    private String region;
    private String city;
    private String street;
    private String house;

    public Address() {
    }

    public Address(String index, String region, String city, String street, String house) {
        this.index = index;
        this.region = region;
        this.city = city;
        this.street = street;
        this.house = house;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("index", index);
        serializer.write("region", region);
        serializer.write("city", city);
        serializer.write("street", street);
        serializer.write("house", house);
    }

    @Override
    public void update(Deserializer deserializer) {
        this.index = deserializer.readString("index");
        this.region = deserializer.readString("region");
        this.city = deserializer.readString("city");
        this.street = deserializer.readString("street");
        this.house = deserializer.readString("house");
    }
}