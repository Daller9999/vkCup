package com.sunplacestudio.vkcupmaps;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

public class MarkerInfo {

    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_EVENT = 2;

    private String httpView;
    private String httpPhoto;
    private String title;
    private int type;
    private String stringType;
    private int id;
    private double latitude;
    private double longitude;
    private Bitmap bitmap;
    private Bitmap bitmapView;
    private LatLng latLng;
    private String address;

    private double latMeters;
    private double longMeters;

    public MarkerInfo(String title, String httpPhoto, String httpView, int type, int id, double latitude, double longitude) {
        this.httpPhoto = httpPhoto;
        this.title = title;
        this.type = type;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.latLng = new LatLng(latitude, longitude);
        this.latMeters = latitude * 111 * 1000;
        this.longMeters = longitude * 111 * 1000;
        this.httpView = httpView;
    }

    public void setAddress(String address) { this.address = address; }

    public String getAddress() { return address; }

    public int getType() { return type; }

    public String getHttpView() { return httpView; }

    public void setBitmapView(Bitmap bitmapView) { this.bitmapView = bitmapView; }

    public Bitmap getBitmapView() { return bitmapView; }

    public double getLatMeters() { return latMeters; }

    public double getLongMeters() { return longMeters; }

    public LatLng getLatLng() { return latLng; }

    public Bitmap getBitmap() { return bitmap; }

    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }

    public String getHttpPhoto() { return httpPhoto; }

    public int getId() { return id; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public String getTitle() { return title; }
}
