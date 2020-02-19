package com.example.vkcupalbums;

import android.graphics.Bitmap;

public class PhotoInfo {

    private boolean isRemove = false;
    private Bitmap bitmap;
    private int id;

    public PhotoInfo(Bitmap bitmap, int id) {
        this.bitmap = bitmap;
        this.id = id;
    }

    public Bitmap getBitmap() { return bitmap; }

    public void setRemove() {
        isRemove = true;
    }

    public void setRemove(boolean remove) { isRemove = remove; }

    public boolean isRemove() { return isRemove; }

    public int getId() { return id; }
}
