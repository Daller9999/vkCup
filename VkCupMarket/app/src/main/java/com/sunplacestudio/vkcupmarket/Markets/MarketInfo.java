package com.sunplacestudio.vkcupmarket.Markets;

import android.graphics.Bitmap;

public class MarketInfo {

    private String name;
    private int id;
    private String photoUrl;
    private Bitmap bitmap;
    private int cityId = -1;
    private String cityName = null;
    private static final String[] types = {"Открытая группа", "Закрытая группа", "Частная группа"};
    private String type;

    public MarketInfo(String name, int id, String photoUrl, int type) {
        this.name = name;
        this.id = id;
        this.photoUrl = photoUrl;
        if (type > types.length) type = 0;
        this.type = types[type];
    }

    public Bitmap getBitmap() { return bitmap; }

    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }

    public int getId() { return id; }

    public String getName() { return name; }

    public String getPhotoUrl() { return photoUrl; }

    public void setCity(String name, int id) {
        cityName = name;
        cityId = id;
    }

    public int getCityId() { return cityId; }

    public String getCityName() { return cityName; }

    public String getType() { return type; }
}
