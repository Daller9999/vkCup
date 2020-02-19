package com.example.vkcupalbums;

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
    private boolean shake = false;

    public AlbumInfo(String description, int id, Bitmap bitmap, int count) {
        bitmapMain = bitmap;
        this.id = id;
        this.description = description;
        this.photoCount = count;
    }

    public void setShake(boolean b) {
        shake = b;
    }

    public boolean isShake() {
        return shake && !isRemove;
    }

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
