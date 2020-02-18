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

    private OnPhotoLoad onPhotoLoad;

    public AlbumInfo(String description, int id, String httpMain, int count, OnPhotoLoad onPhotoLoad) {
        new DownloadImageTask(httpMain).execute();
        this.id = id;
        this.description = description;
        this.photoCount = count;
        this.onPhotoLoad = onPhotoLoad;
    }

    public void setRemove(boolean remove) { isRemove = remove; }

    public boolean isRemove() { return isRemove; }

    public String getDescription() { return description; }

    public int getId() { return id; }

    public Bitmap getBitmapMain() { return bitmapMain; }

    public int getPhotoCount() { return photoCount; }

    public String getPhotoCountString() { return String.valueOf(photoCount) + " фотографий"; }

    private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
        String http;

        DownloadImageTask(String http) {
            this.http = http;
        }

        protected Bitmap doInBackground(Void... urls) {
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(http).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("mesUri", "error to load image : " + e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bitmapMain = result;
            if (onPhotoLoad != null)
                onPhotoLoad.onLoad();
        }
    }

    public interface OnPhotoLoad {
        void onLoad();
    }
}
