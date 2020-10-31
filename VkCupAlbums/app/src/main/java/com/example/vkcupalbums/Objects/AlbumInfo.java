package com.example.vkcupalbums.Objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

public class AlbumInfo {

    private Bitmap bitmapMain;
    private String description;
    private int photoCount;
    private int id;
    private boolean isRemove = false;
    private String http;

    public AlbumInfo(String description, int id, int count, String http) {
        this.id = id;
        this.description = description;
        this.photoCount = count;
        this.http = http;
    }

    public String getHttp() { return http; }

    public void setBitmapMain(Bitmap bitmapMain) { this.bitmapMain = bitmapMain; }

    public void setRemove() {
        isRemove = true;
    }

    public void setRemove(boolean remove) { isRemove = remove; }

    public boolean isRemove() { return isRemove; }

    public String getDescription() { return description; }

    public int getId() { return id; }

    public Bitmap getBitmapMain() { return bitmapMain; }

    public int getPhotoCount() { return photoCount; }

    public String getPhotoCountString() { return String.valueOf(photoCount) + " фотографий"; }
}
