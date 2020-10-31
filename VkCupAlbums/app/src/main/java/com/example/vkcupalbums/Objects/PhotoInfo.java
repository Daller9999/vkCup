package com.example.vkcupalbums.Objects;

import android.graphics.Bitmap;

import com.vk.sdk.api.model.VKApiPhoto;

public class PhotoInfo {

    private boolean isRemove = false;
    private Bitmap bitmap;
    private int id;
    private VKApiPhoto vkApiPhoto;

    public PhotoInfo(int id, VKApiPhoto vkApiPhoto) {
        this.id = id;
        this.vkApiPhoto = vkApiPhoto;
    }

    public VKApiPhoto getVkApiPhoto() { return vkApiPhoto; }

    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }

    public Bitmap getBitmap() { return bitmap; }

    public void setRemove() {
        isRemove = true;
    }

    public void setRemove(boolean remove) { isRemove = remove; }

    public boolean isRemove() { return isRemove; }

    public int getId() { return id; }
}
