package models;

import models.serialization.Deserializer;
import models.serialization.Serializable;
import models.serialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 18.01.13
 * Time: 19:27
 */
public class Address implements Serializable {

    private String index;
    private String city;
    private String street;
    private String house;

    public Address(String index, String city, String street, String house) {
        this.index = index;
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
    public void store(Serializer serializer) {
        serializer.write("index", index);
        serializer.write("city", city);
        serializer.write("street", street);
        serializer.write("house", house);
    }

    public static Address deserialize(Deserializer deserializer) {
        return new Address(
                deserializer.getString("index"),
                deserializer.getString("city"),
                deserializer.getString("street"),
                deserializer.getString("house")
        );
    }

    @Override
    public String toString() {
        return "index='" + index + '\'' +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", house='" + house + '\'';
    }
}