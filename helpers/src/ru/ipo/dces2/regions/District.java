package ru.ipo.dces2.regions;

public class District {

    private String name;
    private String code;

    public District(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
