package models;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 18.01.13
 * Time: 19:27
 */
public class Address extends HashMap<String, String> {

    public static final String INDEX = "index";
    public static final String CITY = "city";
    public static final String STREET = "street";
    public static final String HOUSE = "house";

    public Address(String index, String city, String street, String house) {
        setIndex(index);
        setCity(city);
        setStreet(street);
        setHouse(house);
    }

    public String getIndex() {
        return get(INDEX);
    }

    public String getCity() {
        return get(CITY);
    }

    public String getStreet() {
        return get(STREET);
    }

    public String getHouse() {
        return get(HOUSE);
    }

    public void setIndex(String value) {
        put(INDEX, value);
    }

    public void setCity(String value) {
        put(CITY, value);
    }

    public void setStreet(String value) {
        put(STREET, value);
    }

    public void setHouse(String value) {
        put(HOUSE, value);
    }

}
