package com.sunplacestudio.vkcupmarket.Markets;

public class CityInfo {

    private String name;
    private int id;
    private boolean checked = false;

    public CityInfo(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public boolean isChecked() { return checked; }

    public void setChecked(boolean checked) { this.checked = checked; }
}
