package com.example.vkcupalbums.Objects;

import android.graphics.Bitmap;

public class PhotoInfo {

    private boolean isRemove = false;
    private Bitmap bitmap;
    private int id;

    public PhotoInfo(int id) {
        this.id = id;
    }

    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }

    public Bitmap getBitmap() { return bitmap; }

    public void setRemove() {
        isRemove = true;
    }

    public void setRemove(boolean remove) { isRemove = remove; }

    public boolean isRemove() { return isRemove; }

    public int getId() { return id; }
}
