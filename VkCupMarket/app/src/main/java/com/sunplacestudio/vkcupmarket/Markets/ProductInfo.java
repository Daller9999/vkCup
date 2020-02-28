package com.sunplacestudio.vkcupmarket.Markets;

import android.graphics.Bitmap;

public class ProductInfo {

    private String cost;
    private int id;
    private String name;
    private String httpBitmap;
    private Bitmap bitmap;
    private String description;
    private boolean isFavorite;

    public ProductInfo(String name, int id, String httpBitmap, String cost, String description, boolean isFavorite) {
        this.name = name;
        this.id = id;
        this.httpBitmap = httpBitmap;
        this.cost = cost;
        this.description = description;
        this.isFavorite = isFavorite;
    }

    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isFavorite() { return isFavorite; }

    public int getId() { return id; }

    public String getHttpBitmap() { return httpBitmap; }

    public Bitmap getBitmap() { return bitmap; }

    public String getName() { return name; }

    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }

    public String getCost() { return cost; }

    public String getDescription() { return description; }
}
