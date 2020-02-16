package com.sunplacestudio.vkgroupscup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GroupInfo {
    private int id;
    private String httpPhoto;
    private String description;
    private String name;
    private Bitmap bitmap;
    private int members;
    private String stringMembersAndFriends;
    private int friends;
    private String dateString;
    private boolean isSelected;
    private static final String[] months = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня", "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"};

    public GroupInfo(int id, String name, String httpPhoto, String description, int members) {
        this.id = id;
        this.name = name;
        this.httpPhoto = httpPhoto;
        new DownloadImageTask(httpPhoto).execute();
        this.description = description;
        this.members = members;
        stringMembersAndFriends = convertText(members) + " · ";
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getStringMembersAndFriends() {
        return stringMembersAndFriends;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getId() {
        return id;
    }

    public int getMembers() {
        return members;
    }

    public int getFriends() {
        return friends;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setFriends(int friends) {
        this.friends = friends;
        stringMembersAndFriends += String.valueOf(friends) + " друзей";
    }

    void setLastPostDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
        String dateText = dateFormat.format(date);
        String[] split = dateText.split(":");
        dateString = "Последняя запись " + split[0] + " " + months[Integer.valueOf(split[1]) - 1];
    }

    public String getDateString() {
        return dateString;
    }

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
            bitmap = result;
        }
    }

    private String convertText(int count) {
        if (count < 100_000)
            return String.valueOf(count) + " подписчиков";
        else {
            String text = "К";
            String num = String.valueOf(count);
            String res;
            if (count < 1_000_000) {
                res = num.substring(0, 3) + "," + num.substring(3, 4) + "К";
            } else if (count < 10_000_000) {
                res = num.substring(0, 1) + "," + num.substring(1, 4) + "M";
            } else
                res = num.substring(0, 2) + "," + num.substring(2, 5) + "M";
            return res + " подписчиков";

        }

    }
}

